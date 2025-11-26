package microservicio.contacto.controller;

import microservicio.contacto.model.Contacto;
import microservicio.contacto.service.ContactoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/contacto")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Contacto", description = "Operaciones para gestionar contactos y reportes")
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    @Operation(summary = "Guardar contacto", description = "Crea un nuevo contacto. Body: nombre, email, mensaje.")
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

    @Operation(summary = "Listar contactos", description = "Devuelve la lista de contactos registrados.")
    @GetMapping("/listar")
    public ResponseEntity<Iterable<Contacto>> listar() {
        return ResponseEntity.ok(contactoService.listarContactos());
    }

    @Operation(summary = "Eliminar contacto", description = "Elimina el contacto indicado por su id numérico.")
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
