package microservicio.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    private Long id;
    private String rut;
    private String nombre;
    private String fechaNac;
    private String correo;
    private String nombreUsuario;
    private boolean isAdmin;
    private String createdAt;
}
