package microservicio.usuarios.repository;

import microservicio.usuarios.model.Usuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuarios-repo-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UsuariosRepositoryTest {

    @Autowired
    private UsuariosRepository repo;

    @Test
    void saveAndFindByNombreUsuario() {
        Usuarios u = Usuarios.builder().rut("1-1").nombre("Juan").correo("j@x").nombreUsuario("juan").password("p").build();
        Usuarios saved = repo.save(u);
        assertThat(saved.getId()).isNotNull();

        var found = repo.findByNombreUsuario("juan");
        assertThat(found).isPresent();
        assertThat(found.get().getCorreo()).isEqualTo("j@x");
    }
}
