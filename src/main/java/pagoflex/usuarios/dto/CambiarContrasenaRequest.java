package pagoflex.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class CambiarContrasenaRequest {
     @NotBlank
    private String contrasenaActual;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._-]).{8,}$",
        message = "La nueva contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
    )
    private String contrasenaNueva;
}
