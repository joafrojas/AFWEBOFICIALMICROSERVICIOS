package microservicio.foro.config;

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
                        .title("Microservicio Foro - API")
                        .version("v1")
                        .description("API para gestionar publicaciones y comentarios del foro. Documentación en español.")
                        .contact(new Contact().name("Equipo AFWEBO").email("soporte@example.com"))
                );
    }
}
