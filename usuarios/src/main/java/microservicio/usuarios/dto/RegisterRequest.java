package microservicio.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String rut;

    @NotBlank
    private String nombre;

    @JsonAlias({"fecha_nac", "fechaNac"})
    private String fechaNac;

    @NotBlank @Email
    private String correo;

    @NotBlank
    @JsonAlias({"nombre_usu", "nombreUsuario"})
    private String nombreUsuario;

    @NotBlank @Size(min = 6)
    private String password;

    // token opcional enviado por frontend para obtener rol admin
    private String adminToken;
}
