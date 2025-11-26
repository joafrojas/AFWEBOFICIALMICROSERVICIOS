package microservicio.usuarios.controller;

import microservicio.usuarios.dto.UsuarioDto;
import microservicio.usuarios.model.Usuarios;
import microservicio.usuarios.repository.UsuariosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuariosController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuariosRepository repo;

    @Test
    public void listUsers_deberiaRetornarListaVaciaOConUsuarios() throws Exception {
        Usuarios u = Usuarios.builder().id(1L).nombre("Juan").nombreUsuario("juan").correo("j@x.com").rut("1-9").build();
        when(repo.findAll()).thenReturn(java.util.List.of(u));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("juan"));
    }

    @Test
    public void getUserById_debeRetornarUsuario() throws Exception {
        Usuarios u = Usuarios.builder().id(2L).nombre("Ana").nombreUsuario("ana").correo("a@x.com").rut("2-9").build();
        when(repo.findById(2L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("ana"));
    }

    @Test
    public void setAdmin_debeRechazarSinToken() throws Exception {
        mockMvc.perform(put("/users/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
