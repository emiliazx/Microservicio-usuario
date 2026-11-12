package pagoflex.usuarios.service;

import pagoflex.usuarios.dto.*;
import pagoflex.usuarios.model.Usuario;
import pagoflex.usuarios.repository.UsuarioRepository;
import pagoflex.usuarios.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final int MAX_INTENTOS = 5;

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository repository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest req) {
        String correo = req.getCorreo().toLowerCase(Locale.ROOT).trim();
        Usuario u = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (u.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Cuenta bloqueada por demasiados intentos fallidos. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(req.getContrasena(), u.getClaveHash())) {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if (u.getIntentosFallidos() >= MAX_INTENTOS) {
                u.setBloqueado(true);
            }
            repository.save(u);
            int restantes = MAX_INTENTOS - u.getIntentosFallidos();
            String msg = restantes > 0
                    ? "Credenciales inválidas. Intentos restantes: " + restantes
                    : "Cuenta bloqueada por demasiados intentos fallidos.";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg);
        }

        // Login exitoso → resetear intentos
        u.setIntentosFallidos(0);
        repository.save(u);

        String token = jwtService.generarToken(u.getId(), u.getCorreo(), u.getRol());
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(u.getId())
                .nombre(u.getNombre())
                .rut(u.getRut())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .tieneFoto(u.getFoto() != null && u.getFoto().length > 0)
                .build();
    }

    public UsuarioResponse registrar(RegistroRequest req) {
        String correo = req.getCorreo().toLowerCase(Locale.ROOT).trim();
        if (repository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        Usuario u = Usuario.builder()
                .nombre(req.getNombre().trim())
                .rut(req.getRut().trim())
                .correo(correo)
                .claveHash(passwordEncoder.encode(req.getContrasena())) // BCrypt + sal
                .rol("USUARIO")
                .pregunta1(req.getPregunta1())
                .respuesta1Hash(req.getRespuesta1() != null ? hashSimple(normalizar(req.getRespuesta1())) : "")
                .pregunta2(req.getPregunta2())
                .respuesta2Hash(req.getRespuesta2() != null ? hashSimple(normalizar(req.getRespuesta2())) : "")
                .intentosFallidos(0)
                .bloqueado(false)
                .build();
        return toResponse(repository.save(u));
    }

    public UsuarioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    public List<UsuarioResponse> listar(String buscar) {
        List<Usuario> lista = (buscar == null || buscar.isBlank())
                ? repository.findAll()
                : repository.findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(buscar, buscar);
        return lista.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        Usuario u = buscar(id);
        if (req.getNombre() != null) u.setNombre(req.getNombre().trim());
        if (req.getRut() != null) u.setRut(req.getRut().trim());
        if (req.getPregunta1() != null) u.setPregunta1(req.getPregunta1());
        if (req.getRespuesta1() != null) u.setRespuesta1Hash(hashSimple(normalizar(req.getRespuesta1())));
        if (req.getPregunta2() != null) u.setPregunta2(req.getPregunta2());
        if (req.getRespuesta2() != null) u.setRespuesta2Hash(hashSimple(normalizar(req.getRespuesta2())));
        return toResponse(repository.save(u));
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest req) {
        Usuario u = buscar(id);
        if (!passwordEncoder.matches(req.getContrasenaActual(), u.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
        }
        u.setClaveHash(passwordEncoder.encode(req.getContrasenaNueva()));
        repository.save(u);
    }

    public void recuperar(RecuperarRequest req) {
        Usuario u = repository.findByCorreo(req.getCorreo().toLowerCase(Locale.ROOT).trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!hashSimple(normalizar(req.getRespuesta1())).equals(u.getRespuesta1Hash())
                || !hashSimple(normalizar(req.getRespuesta2())).equals(u.getRespuesta2Hash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Respuestas incorrectas");
        }
        // Validar complejidad de la nueva clave
        String nueva = req.getNuevaContrasena();
        if (nueva == null || !nueva.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._-]).{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial");
        }
        u.setClaveHash(passwordEncoder.encode(nueva));
        u.setIntentosFallidos(0);
        u.setBloqueado(false);
        repository.save(u);
    }

    /** Desbloquear cuenta (solo ADMIN) */
    public void desbloquear(Long id) {
        Usuario u = buscar(id);
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        repository.save(u);
    }

    public void guardarFoto(Long id, byte[] foto) {
        Usuario u = buscar(id);
        u.setFoto(foto);
        repository.save(u);
    }

    public byte[] obtenerFoto(Long id) {
        Usuario u = buscar(id);
        if (u.getFoto() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin foto");
        }
        return u.getFoto();
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        repository.deleteById(id);
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .rut(u.getRut())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .pregunta1(u.getPregunta1())
                .pregunta2(u.getPregunta2())
                .tieneFoto(u.getFoto() != null && u.getFoto().length > 0)
                .build();
    }

    /** Hash simple SHA-256 solo para respuestas secretas (no contraseñas) */
    public static String hashSimple(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
