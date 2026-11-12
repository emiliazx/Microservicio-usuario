package pagoflex.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String rut;
    private String correo;
    private String rol;
    private String pregunta1;
    private String pregunta2;
    private boolean tieneFoto;
}
