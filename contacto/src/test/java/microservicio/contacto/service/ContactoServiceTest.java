package microservicio.contacto.service;

import microservicio.contacto.model.Contacto;
import microservicio.contacto.repository.ContactoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;

public class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    @InjectMocks
    private ContactoService contactoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarContacto_deberiaValidarNombre() {
        Contacto c = new Contacto(null, "  ", "a@b.com", "mensaje suficientemente largo");
        Assertions.assertThrows(IllegalArgumentException.class, () -> contactoService.guardarContacto(c));
    }

    @Test
    void guardarContacto_deberiaValidarEmail() {
        Contacto c = new Contacto(null, "Juan", "no-email", "mensaje suficientemente largo");
        Assertions.assertThrows(IllegalArgumentException.class, () -> contactoService.guardarContacto(c));
    }

    @Test
    void guardarContacto_deberiaValidarMensaje() {
        Contacto c = new Contacto(null, "Ana", "ana@x.com", "corto");
        Assertions.assertThrows(IllegalArgumentException.class, () -> contactoService.guardarContacto(c));
    }

    @Test
    void guardarContacto_deberiaGuardarCorrectamente() {
        Contacto input = new Contacto(null, "Pedro", "p@x.com", "mensaje con longitud suficiente");
        Contacto saved = new Contacto(1L, "Pedro", "p@x.com", "mensaje con longitud suficiente");
        Mockito.when(contactoRepository.save(any())).thenReturn(saved);

        Contacto result = contactoService.guardarContacto(input);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("Pedro", result.getNombre());
    }

    @Test
    void eliminarContacto_deberiaLanzarSiNoExiste() {
        Mockito.when(contactoRepository.existsById(5L)).thenReturn(false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> contactoService.eliminarContacto(5L));
    }

    @Test
    void eliminarContacto_deberiaEliminarSiExiste() {
        Mockito.when(contactoRepository.existsById(2L)).thenReturn(true);
        Mockito.doNothing().when(contactoRepository).deleteById(2L);
        // No debe lanzar
        contactoService.eliminarContacto(2L);
        Mockito.verify(contactoRepository).deleteById(2L);
    }
}
