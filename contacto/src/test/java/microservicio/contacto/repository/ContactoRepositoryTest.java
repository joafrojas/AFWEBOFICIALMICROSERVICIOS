package microservicio.contacto.repository;

import microservicio.contacto.model.Contacto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ContactoRepositoryTest {

    @Autowired
    private ContactoRepository contactoRepository;

    @Test
    void saveAndFindAll_debePersistirContacto() {
        Contacto c = new Contacto(null, "Test", "t@x.com", "mensaje largo de prueba");
        Contacto saved = contactoRepository.save(c);
        Assertions.assertNotNull(saved.getId());

        var all = contactoRepository.findAll();
        Assertions.assertTrue(all.iterator().hasNext());
    }

    @Test
    void existsById_funciona() {
        Contacto c = new Contacto(null, "Existe", "e@x.com", "mensaje largo de prueba");
        Contacto saved = contactoRepository.save(c);
        Assertions.assertTrue(contactoRepository.existsById(saved.getId()));
    }
}
