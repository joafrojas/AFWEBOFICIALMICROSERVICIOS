package microservicio.foro.controller;

import microservicio.foro.model.Comment;
import microservicio.foro.model.Post;
import microservicio.foro.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173") 
public class ForoController {
    private final PostService service;
    private static final Logger logger = LoggerFactory.getLogger(ForoController.class);

    public ForoController(PostService service) {
        this.service = service;
    }

    @GetMapping
    // Listar todas las publicaciones (resumen: sin content)
        public List<microservicio.foro.model.PostSummary> listarPublicaciones() {
        return service.listarTodas().stream().map(p ->
            new microservicio.foro.model.PostSummary(
                p.getId(),
                p.getExternalId(),
                p.getTitle(),
                p.getImage(),
                p.getCategory(),
                p.getCreatedAt()
            )
        ).toList();
        }

    @GetMapping("/{id}")
    // Obtener una publicación por id numérico o external id (ej: p2)
    public ResponseEntity<Post> obtenerPublicacion(@PathVariable String id) {
        return service.buscarPorIdentificador(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    // Crear una nueva publicación
    public ResponseEntity<Post> crearPublicacion(@RequestBody Post p) {
        logger.info("ForoController: crearPublicacion - recibida publicación: title='{}', authorId='{}'", p.getTitle(), p.getAuthorId());
        Post created = service.crear(p);
        logger.info("ForoController: crearPublicacion - creada publicación id={} externalId={}", created.getId(), created.getExternalId());
        return ResponseEntity.ok(created);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Crear una nueva publicación con subida de archivo (multipart)
        public ResponseEntity<Post> crearPublicacionMultipart(
            @RequestParam("title") String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "authorId", required = false) String authorId,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
        ) {
        Post p = new Post();
        p.setTitle(title);
        // No se maneja campo 'content' en este servicio (se eliminó).
        p.setCategory(category);
        p.setAuthorId(authorId == null ? "ANON" : authorId);

        // Guardar archivo si llega
        if (imageFile != null && !imageFile.isEmpty()) {
                try {
                // Guardar en carpeta externa 'uploads' en la raíz del proyecto
                Path projectRoot = Paths.get("").toAbsolutePath();
                Path uploadDir = projectRoot.resolve("uploads");
                Files.createDirectories(uploadDir);
                String original = imageFile.getOriginalFilename();
                String ext = "";
                if (original != null) {
                    int dot = original.lastIndexOf('.');
                    if (dot >= 0) ext = original.substring(dot);
                }
                String filename = UUID.randomUUID().toString() + ext;
                Path dest = uploadDir.resolve(filename);
                // transferTo(Path) puede aceptar dest
                // transferTo may expect a non-null Path; convert to File as safe fallback
                try {
                    imageFile.transferTo(dest.toFile());
                } catch (NoSuchMethodError err) {
        
                    Files.write(dest, imageFile.getBytes());
                }
                // Guardamos la URL pública completa al archivo en el backend
                // Construir URL pública basada en el contexto actual (es más robusto que hardcodear host)
                String publicUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(filename)
                        .toUriString();
                p.setImage(publicUrl);
            } catch (IOException e) {
                logger.error("Error guardando archivo de imagen", e);
                return ResponseEntity.status(500).build();
            }
        }

        logger.info("ForoController: crearPublicacionMultipart - recibida title='{}' authorId='{}' imageFile={}", title, authorId, imageFile == null ? 0 : imageFile.getSize());
        Post created = service.crear(p);
        logger.info("ForoController: crearPublicacionMultipart - creada id={} externalId={}", created.getId(), created.getExternalId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/full")
    // Listar todas las publicaciones (completo, incluyendo content e imagen completa)
    public List<Post> listarPublicacionesFull() {
        return service.listarTodas();
    }

    @PostMapping("/admin/fix-categories")
    // Admin: corregir categorías para publicaciones conocidas (p8,p9,p10 => COLECCIONES)
    public ResponseEntity<List<Post>> fixCategories() {
        String[] ensureCollections = {"p8", "p9", "p10"};
        List<Post> updated = new java.util.ArrayList<>();
        for (String ext : ensureCollections) {
            service.buscarPorExternalId(ext).ifPresent(p -> {
                p.setCategory("COLECCIONES");
                Post saved = service.crear(p);
                updated.add(saved);
            });
        }
        return ResponseEntity.ok(updated);
    }

    

    @DeleteMapping("/{id}")
    // Eliminar una publicación por identificador
    public ResponseEntity<?> eliminarPublicacion(@PathVariable String id) {
        return service.buscarPorIdentificador(id).map(p -> {
            service.eliminar(p.getId());
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/comments")
    // Agregar comentario a una publicación
    public ResponseEntity<Comment> agregarComentario(@PathVariable String id, @RequestBody Comment c) {
        return service.buscarPorIdentificador(id).map(p -> {
            Comment created = service.agregarComentario(p.getId(), c);
            return ResponseEntity.ok(created);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/comments/{cid}")
    // Eliminar comentario
    public ResponseEntity<?> eliminarComentario(@PathVariable String id, @PathVariable Long cid) {
        return service.buscarPorIdentificador(id).map(p -> {
            service.eliminarComentario(p.getId(), cid);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/likes")
    // Alternar like de un usuario en la publicación
    public ResponseEntity<Post> alternarLike(@PathVariable String id, @RequestBody String userId) {
        return service.buscarPorIdentificador(id).map(p -> {
            Post updated = service.alternarLike(p.getId(), userId);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/cleanup-user/{userId}")
    // Admin: limpiar referencias de un usuario en las publicaciones (likes y comentarios)
    public ResponseEntity<?> cleanupUserReferences(@PathVariable String userId) {
        try {
            java.util.Map<String,Integer> result = service.removerReferenciasUsuario(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error limpiando referencias de usuario {}", userId, e);
            return ResponseEntity.status(500).body("Error limpiando referencias");
        }
    }
}
