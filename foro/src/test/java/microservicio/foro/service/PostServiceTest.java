package microservicio.foro.service;

import microservicio.foro.model.Post;
import microservicio.foro.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PostServiceTest {

    @Mock
    private PostRepository repo;

    @InjectMocks
    private PostService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crear_siNoTraeExternalId_generarYGuardar() {
        Post p = Post.builder().title("t").authorId("a").build();
        when(repo.findByExternalId(any())).thenReturn(Optional.empty());
        when(repo.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post out = service.crear(p);
        assertThat(out.getExternalId()).isNotBlank();
    }

    @Test
    void alternarLike_agregaYRemueve() {
        Post p = Post.builder().id(1L).title("t").authorId("a").build();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post res1 = service.alternarLike(1L, "u1");
        assertThat(res1.getLikes()).contains("u1");

        Post res2 = service.alternarLike(1L, "u1");
        assertThat(res2.getLikes()).doesNotContain("u1");
    }
}
