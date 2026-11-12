package pagoflex.usuarios.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.usuarios.dto.ActualizarUsuarioRequest;
import pagoflex.usuarios.dto.CambiarContrasenaRequest;
import pagoflex.usuarios.dto.LoginRequest;
import pagoflex.usuarios.dto.LoginResponse;
import pagoflex.usuarios.dto.RecuperarRequest;
import pagoflex.usuarios.dto.RegistroRequest;
import pagoflex.usuarios.dto.UsuarioResponse;
import pagoflex.usuarios.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Autenticación JWT, registro y perfil")
public class UsuarioController {
     private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión → devuelve JWT Bearer")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario (público)")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar) {
        return service.listar(buscar);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar contraseña (requiere actual + nueva con complejidad)")
    public void cambiarContrasena(@PathVariable Long id, @Valid @RequestBody CambiarContrasenaRequest req) {
        service.cambiarContrasena(id, req);
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Recuperar cuenta con preguntas secretas")
    public void recuperar(@Valid @RequestBody RecuperarRequest req) {
        service.recuperar(req);
    }

    @PutMapping("/{id}/desbloquear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desbloquear cuenta (solo ADMIN)")
    public void desbloquear(@PathVariable Long id) {
        service.desbloquear(id);
    }

    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void subirFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile archivo) throws Exception {
        service.guardarFoto(id, archivo.getBytes());
    }

    @GetMapping(value = "/{id}/foto", produces = MediaType.IMAGE_JPEG_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerFoto(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}

    

