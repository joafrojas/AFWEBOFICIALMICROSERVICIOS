package microservicio.foro.controller;

import microservicio.foro.model.Post;
import microservicio.foro.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForoController.class)
public class ForoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService service;

    @Test
    public void listarPublicaciones_deberiaRetornarResumen() throws Exception {
        Post p = Post.builder().id(1L).externalId("p1").title("Hola").image(null).category("GEN").build();
        when(service.listarTodas()).thenReturn(java.util.List.of(p));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("p1"));
    }

    @Test
    public void crearPublicacion_deberiaCrear() throws Exception {
        Post toCreate = Post.builder().title("Nuevo").authorId("u1").build();
        Post created = Post.builder().id(2L).externalId("p2").title("Nuevo").authorId("u1").build();
        when(service.crear(any())).thenReturn(created);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nuevo\",\"authorId\":\"u1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }
}
