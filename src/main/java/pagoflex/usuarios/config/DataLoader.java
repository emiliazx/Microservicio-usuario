package pagoflex.usuarios.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import pagoflex.usuarios.model.Usuario;
import pagoflex.usuarios.repository.UsuarioRepository;
import pagoflex.usuarios.service.UsuarioService;
@Component 
public class DataLoader implements CommandLineRunner {
     private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        repository.save(Usuario.builder()
                .nombre("Administrador").rut("11111111-1").correo("admin@pagoflex.cl")
                .claveHash(passwordEncoder.encode("Admin123$")).rol("ADMIN")
                .pregunta1("¿Nombre de tu primera mascota?").respuesta1Hash(UsuarioService.hashSimple("firulais"))
                .pregunta2("¿Nombre de tu madre?").respuesta2Hash(UsuarioService.hashSimple("maria"))
                .intentosFallidos(0).bloqueado(false)
                .build());

        repository.save(Usuario.builder()
                .nombre("Gestor de Deudas").rut("13579246-2").correo("gestor@pagoflex.cl")
                .claveHash(passwordEncoder.encode("Gestor123$")).rol("GESTOR")
                .pregunta1("¿Nombre de tu primera mascota?").respuesta1Hash(UsuarioService.hashSimple("firulais"))
                .pregunta2("¿Nombre de tu madre?").respuesta2Hash(UsuarioService.hashSimple("maria"))
                .intentosFallidos(0).bloqueado(false)
                .build());

        repository.save(Usuario.builder()
                .nombre("Emilia Zamora").rut("12345678-5").correo("emilia@pagoflex.cl")
                .claveHash(passwordEncoder.encode("Emilia123$")).rol("USUARIO")
                .pregunta1("¿Nombre de tu primera mascota?").respuesta1Hash(UsuarioService.hashSimple("firulais"))
                .pregunta2("¿Nombre de tu madre?").respuesta2Hash(UsuarioService.hashSimple("maria"))
                .intentosFallidos(0).bloqueado(false)
                .build());
    }
}
    

