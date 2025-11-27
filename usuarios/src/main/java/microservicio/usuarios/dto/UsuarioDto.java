package microservicio.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("rut")
    private String rut;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("fechaNac")
    private String fechaNac;

    @JsonProperty("correo")
    private String correo;

    // nombreUsuario es el campo que usa el frontend para mostrar el username
    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    // Forzar el nombre JSON a isAdmin para que el frontend lo detecte sin ambiguedades
    @JsonProperty("isAdmin")
    private boolean isAdmin;

    @JsonProperty("rol")
    private String rol;

    @JsonProperty("createdAt")
    private String createdAt;
}
