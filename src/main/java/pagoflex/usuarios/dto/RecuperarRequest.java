package pagoflex.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class RecuperarRequest {
    @NotBlank @Email
    private String correo;
    @NotBlank
    private String respuesta1;
    @NotBlank
    private String respuesta2;
    @NotBlank
    private String nuevaContrasena;
}

