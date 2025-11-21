package microservicio.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    @JsonAlias({"usuario", "username", "email", "usernameOrEmail"})
    private String usernameOrEmail;
    @NotBlank
    private String password;
}
