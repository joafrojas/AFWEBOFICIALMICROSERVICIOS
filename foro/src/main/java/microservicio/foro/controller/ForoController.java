package microservicio.foro.controller;

import microservicio.foro.model.Comment;
import microservicio.foro.model.Post;
import microservicio.foro.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", exposedHeaders = "X-ADMIN-TOKEN")
@Tag(name = "Foro", description = "Operaciones para gestionar publicaciones, comentarios y likes")
public class ForoController {
    private final PostService service;
    private static final Logger logger = LoggerFactory.getLogger(ForoController.class);

    // NOTE: No local admin token fallback — frontend must send X-ADMIN-TOKEN header.
    @org.springframework.beans.factory.annotation.Value("${usuarios.service.url:http://localhost:8081/usuarios-api}")
    private String usuariosServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${usuarios.service.token:}")
    private String usuariosServiceToken;
    @org.springframework.beans.factory.annotation.Value("${usuarios.service.username:}")
    private String usuariosServiceUsername;
    @org.springframework.beans.factory.annotation.Value("${usuarios.service.password:}")
    private String usuariosServicePassword;

    public ForoController(PostService service) {
        this.service = service;
    }

    @Operation(summary = "Listar publicaciones (resumen)", description = "Devuelve resumen de publicaciones sin contenido completo.")
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

    @Operation(summary = "Obtener publicación", description = "Obtiene una publicación por id numérico o external id (ej: p2).")
    @GetMapping("/{id}")
    // Obtener una publicación por id numérico o external id (ej: p2)
    public ResponseEntity<Post> obtenerPublicacion(@PathVariable String id) {
        return service.buscarPorIdentificador(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear publicación", description = "Crea una nueva publicación (JSON).")
    @PostMapping
    // Crear una nueva publicación
    public ResponseEntity<Post> crearPublicacion(@RequestBody Post p) {
        // Normalizar valores opcionales para aceptar payloads mínimos
        if (p == null) return ResponseEntity.badRequest().build();
        if (p.getTitle() == null || p.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (p.getAuthorId() == null || p.getAuthorId().isBlank()) p.setAuthorId("ANON");
        if (p.getLikes() == null) p.setLikes(new java.util.ArrayList<>());
        if (p.getComments() == null) p.setComments(new java.util.ArrayList<>());

        logger.info("ForoController: crearPublicacion - recibida publicación: title='{}', authorId='{}'", p.getTitle(), p.getAuthorId());
        try {
            Post created = service.crear(p);
            logger.info("ForoController: crearPublicacion - creada publicación id={} externalId={}", created.getId(), created.getExternalId());
            return ResponseEntity.status(201).body(created);
        } catch (Exception ex) {
            logger.error("Error creando publicación", ex);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Crear publicación multipart", description = "Crea una nueva publicación con posible imagen en multipart/form-data.")
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
        try {
            Post created = service.crear(p);
            logger.info("ForoController: crearPublicacionMultipart - creada id={} externalId={}", created.getId(), created.getExternalId());
            return ResponseEntity.status(201).body(created);
        } catch (Exception ex) {
            logger.error("Error creando publicación multipart", ex);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Listar publicaciones (completo)", description = "Devuelve publicaciones completas incluyendo contenido e imagen.")
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

    

    @Operation(summary = "Eliminar publicación", description = "Elimina una publicación por su identificador numérico o external id.")
    @DeleteMapping("/{id}")
    // Eliminar una publicación por identificador
    public ResponseEntity<?> eliminarPublicacion(@PathVariable String id) {
        return service.buscarPorIdentificador(id).map(p -> {
            service.eliminar(p.getId());
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Agregar comentario", description = "Agrega un comentario a la publicación indicada.")
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

    @Operation(summary = "Proxy: listar usuarios (desde servicio Usuarios)", description = "Obtiene la lista de usuarios desde el microservicio 'usuarios' y la devuelve tal cual.")
    @GetMapping("/admin/users")
    public ResponseEntity<?> proxyListUsers() {
        try {
            HttpClient client = HttpClient.newHttpClient();
                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(usuariosServiceUrl + "/users"))
                    .GET();
                if (usuariosServiceUsername != null && !usuariosServiceUsername.isBlank() && usuariosServicePassword != null) {
                    String cred = java.util.Base64.getEncoder().encodeToString((usuariosServiceUsername + ":" + usuariosServicePassword).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    reqBuilder.header("Authorization", "Basic " + cred);
                } else if (usuariosServiceToken != null && !usuariosServiceToken.isBlank()) {
                    reqBuilder.header("X-ADMIN-TOKEN", usuariosServiceToken);
                }
                HttpRequest req = reqBuilder.build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                List<Object> list = mapper.readValue(resp.body(), new TypeReference<List<Object>>(){});
                return ResponseEntity.ok(list);
            } else {
                return ResponseEntity.status(resp.statusCode()).body(resp.body());
            }
        } catch (Exception e) {
            logger.error("Error proxying list users", e);
            return ResponseEntity.status(500).body("Error obteniendo usuarios");
        }
    }

    @Operation(summary = "Proxy: eliminar usuario (desde servicio Usuarios)", description = "Elimina un usuario en el microservicio 'usuarios'. Requiere header X-ADMIN-TOKEN si el servicio remoto lo exige.")
    @DeleteMapping("/admin/users/{id}")
        public ResponseEntity<?> proxyDeleteUser(@PathVariable Long id, @RequestHeader(value = "X-ADMIN-TOKEN", required = false) String adminToken) {
        try {
            HttpClient client = HttpClient.newHttpClient();
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(usuariosServiceUrl + "/users/" + id))
                    .DELETE();
                // prefer caller token, then basic auth credentials, then admin token
                if (adminToken != null && !adminToken.isBlank()) {
                    builder.header("X-ADMIN-TOKEN", adminToken);
                } else if (usuariosServiceUsername != null && !usuariosServiceUsername.isBlank() && usuariosServicePassword != null) {
                    String cred = java.util.Base64.getEncoder().encodeToString((usuariosServiceUsername + ":" + usuariosServicePassword).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    builder.header("Authorization", "Basic " + cred);
                } else if (usuariosServiceToken != null && !usuariosServiceToken.isBlank()) {
                    builder.header("X-ADMIN-TOKEN", usuariosServiceToken);
                }
            HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                // devolver el body parseado si existe
                if (resp.body() != null && !resp.body().isBlank()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Object obj = mapper.readValue(resp.body(), Object.class);
                    return ResponseEntity.ok(obj);
                }
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(resp.statusCode()).body(resp.body());
            }
        } catch (Exception e) {
            logger.error("Error proxying delete user {}", id, e);
            return ResponseEntity.status(500).body("Error eliminando usuario");
        }
    }

    @Operation(summary = "Proxy: actualizar rol admin para un usuario", description = "Actualiza el flag isAdmin de un usuario en el servicio 'usuarios'. Requiere X-ADMIN-TOKEN si el servicio lo exige.")
    @PutMapping("/admin/users/{id}/admin")
    public ResponseEntity<?> proxySetAdmin(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String,Object> body,
                                          @RequestHeader(value = "X-ADMIN-TOKEN", required = false) String adminToken) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String payload = body == null ? "" : mapper.writeValueAsString(body);

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(usuariosServiceUrl + "/users/" + id + "/admin"))
                    .PUT(payload.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload))
                    .header("Content-Type", "application/json");
                if (adminToken != null && !adminToken.isBlank()) {
                    builder.header("X-ADMIN-TOKEN", adminToken);
                } else if (usuariosServiceUsername != null && !usuariosServiceUsername.isBlank() && usuariosServicePassword != null) {
                    String cred = java.util.Base64.getEncoder().encodeToString((usuariosServiceUsername + ":" + usuariosServicePassword).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    builder.header("Authorization", "Basic " + cred);
                } else if (usuariosServiceToken != null && !usuariosServiceToken.isBlank()) {
                    builder.header("X-ADMIN-TOKEN", usuariosServiceToken);
                }

            HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                if (resp.body() != null && !resp.body().isBlank()) {
                    Object obj = mapper.readValue(resp.body(), Object.class);
                    return ResponseEntity.ok(obj);
                }
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(resp.statusCode()).body(resp.body());
            }
        } catch (Exception e) {
            logger.error("Error proxying setAdmin for user {}", id, e);
            return ResponseEntity.status(500).body("Error actualizando rol admin");
        }
    }
}
