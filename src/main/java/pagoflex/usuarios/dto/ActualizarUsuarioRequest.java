package pagoflex.usuarios.dto;

import lombok.Data;

@Data 
public class ActualizarUsuarioRequest {
    private String nombre;
    private String rut;
    private String pregunta1;
    private String respuesta1;
    private String pregunta2;
    private String respuesta2;
}

