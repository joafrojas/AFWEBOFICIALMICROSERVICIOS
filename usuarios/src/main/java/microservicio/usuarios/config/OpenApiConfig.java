package microservicio.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio Usuarios - API")
                        .version("v1")
                        .description("API para la gestión de usuarios. Documentación en español.")
                        .contact(new Contact().name("Equipo AFWEBO").email("soporte@example.com"))
                );
    }
}
