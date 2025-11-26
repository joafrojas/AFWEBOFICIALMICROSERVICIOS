package microservicio.foro.repository;

import microservicio.foro.model.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:foro-repo-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PostRepositoryTest {

    @Autowired
    private PostRepository repo;

    @Test
    void saveAndFindByExternalId() {
        Post p = Post.builder().title("t").authorId("a").externalId("ext1").build();
        Post saved = repo.save(p);
        assertThat(saved.getId()).isNotNull();

        var f = repo.findByExternalId("ext1");
        assertThat(f).isPresent();
    }
}
