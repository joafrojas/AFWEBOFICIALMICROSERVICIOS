package microservicio.contacto.controller;

import microservicio.contacto.model.Contacto;
import microservicio.contacto.service.ContactoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacto")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    @PostMapping("/guardar")
    public ResponseEntity<?> guardarContacto(@RequestBody Contacto contacto) {
        try {
            Contacto guardado = contactoService.guardarContacto(contacto);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar contacto");
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<Iterable<Contacto>> listar() {
        return ResponseEntity.ok(contactoService.listarContactos());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            contactoService.eliminarContacto(id);
            return ResponseEntity.ok().body(java.util.Map.of("message", "deleted"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error eliminando contacto");
        }
    }
}
