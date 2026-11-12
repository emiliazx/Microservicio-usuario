package pagoflex.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroRequest {
    @NotBlank
    private String nombre;
    @NotBlank
    private String rut;
    @NotBlank @Email
    private String correo;

    
    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._-]).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
    )
    private String contrasena;

    private String pregunta1;
    private String respuesta1;
    private String pregunta2;
    private String respuesta2;
}
