package microservicio.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private boolean isAdmin;
    private String createdAt;
    private String correo;
    private String fechaNac;
    private Long userId;
}
