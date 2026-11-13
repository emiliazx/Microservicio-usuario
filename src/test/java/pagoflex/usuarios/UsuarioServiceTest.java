package pagoflex.usuarios;

import pagoflex.usuarios.dto.*;
import pagoflex.usuarios.model.Usuario;
import pagoflex.usuarios.repository.UsuarioRepository;
import pagoflex.usuarios.security.JwtService;
import pagoflex.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository repository;
    @Mock private JwtService jwtService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UsuarioService service;
    private Usuario demo;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, passwordEncoder, jwtService);
        demo = Usuario.builder()
                .id(1L).nombre("Emilia").rut("12345678-5").correo("emilia@pagoflex.cl")
                .claveHash(passwordEncoder.encode("Emilia123$")).rol("USUARIO")
                .pregunta1("mascota").respuesta1Hash(UsuarioService.hashSimple("firulais"))
                .pregunta2("madre").respuesta2Hash(UsuarioService.hashSimple("maria"))
                .intentosFallidos(0).bloqueado(false)
                .build();
    }

    @Test
    void loginExitosoDevuelveToken() {
        when(repository.findByCorreo("emilia@pagoflex.cl")).thenReturn(Optional.of(demo));
        when(jwtService.generarToken(1L, "emilia@pagoflex.cl", "USUARIO")).thenReturn("jwt-token-demo");
        when(repository.save(any(Usuario.class))).thenReturn(demo);

        LoginRequest req = new LoginRequest();
        req.setCorreo("emilia@pagoflex.cl");
        req.setContrasena("Emilia123$");

        LoginResponse res = service.login(req);
        assertEquals("jwt-token-demo", res.getToken());
        assertEquals("Bearer", res.getTipo());
        assertEquals("Emilia", res.getNombre());
        assertEquals(0, demo.getIntentosFallidos());
    }

    @Test
    void loginFallaYSumaIntentos() {
        when(repository.findByCorreo("emilia@pagoflex.cl")).thenReturn(Optional.of(demo));
        when(repository.save(any(Usuario.class))).thenReturn(demo);

        LoginRequest req = new LoginRequest();
        req.setCorreo("emilia@pagoflex.cl");
        req.setContrasena("mala");

        assertThrows(ResponseStatusException.class, () -> service.login(req));
        assertEquals(1, demo.getIntentosFallidos());
    }

    @Test
    void loginBloqueaTras5Intentos() {
        demo.setIntentosFallidos(4);
        when(repository.findByCorreo("emilia@pagoflex.cl")).thenReturn(Optional.of(demo));
        when(repository.save(any(Usuario.class))).thenReturn(demo);

        LoginRequest req = new LoginRequest();
        req.setCorreo("emilia@pagoflex.cl");
        req.setContrasena("mala");

        assertThrows(ResponseStatusException.class, () -> service.login(req));
        assertTrue(demo.isBloqueado());
    }

    @Test
    void registrarUsuarioNuevoConBCrypt() {
        when(repository.existsByCorreo("nuevo@test.cl")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        RegistroRequest req = new RegistroRequest();
        req.setNombre("Nuevo");
        req.setRut("99999999-9");
        req.setCorreo("nuevo@test.cl");
        req.setContrasena("Clave123$");
        UsuarioResponse res = service.registrar(req);
        assertEquals(10L, res.getId());
        assertEquals("USUARIO", res.getRol());
    }
}
