package microservicio.contacto.controller;

import microservicio.contacto.model.Contacto;
import microservicio.contacto.service.ContactoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactoController.class)
public class ContactoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactoService contactoService;

    @Test
    public void guardarContacto_deberiaCrearContacto() throws Exception {
        Contacto saved = new Contacto(1L, "Juan", "juan@ejemplo.com", "Hola");
        when(contactoService.guardarContacto(any())).thenReturn(saved);

        mockMvc.perform(post("/contacto/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan\",\"email\":\"juan@ejemplo.com\",\"mensaje\":\"Hola\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void listar_deberiaRetornarLista() throws Exception {
        when(contactoService.listarContactos()).thenReturn(java.util.List.of(
                new Contacto(1L, "Ana", "ana@x.com", "msg")
        ));

        mockMvc.perform(get("/contacto/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    public void eliminar_deberiaResponderOk() throws Exception {
        // Simular que no lanza excepción
        org.mockito.Mockito.doNothing().when(contactoService).eliminarContacto(1L);

        mockMvc.perform(delete("/contacto/1"))
                .andExpect(status().isOk());
    }
}
