package pagoflex.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String rut;

    @Column(nullable = false, unique = true)
    private String correo;

    /** Hash BCrypt (incluye sal automática) */
    @Column(name = "clave_hash", nullable = false)
    private String claveHash;

    @Column(nullable = false)
    private String rol; // ADMIN, GESTOR, USUARIO

    private String pregunta1;
    private String respuesta1Hash;
    private String pregunta2;
    private String respuesta2Hash;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;

    /** Intentos fallidos de login */
    @Column(nullable = false)
    @Builder.Default
    private int intentosFallidos = 0;

    /** Cuenta bloqueada temporalmente */
    @Column(nullable = false)
    @Builder.Default
    private boolean bloqueado = false;
}
