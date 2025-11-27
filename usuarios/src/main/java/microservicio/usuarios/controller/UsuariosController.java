package microservicio.usuarios.controller;

import microservicio.usuarios.repository.UsuariosRepository;
import microservicio.usuarios.repository.RoleRepository;
import microservicio.usuarios.dto.UsuarioDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import microservicio.usuarios.model.Rol;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Usuarios", description = "Operaciones para gestión de usuarios y administración")
public class UsuariosController {

    private final UsuariosRepository repo;
    private final RoleRepository roleRepo;

    @Value("${app.admin.token:}")
    private String adminToken;

    public UsuariosController(UsuariosRepository repo, RoleRepository roleRepo) {
        this.repo = repo;
        this.roleRepo = roleRepo;
    }

    // Eliminar usuario por id — requiere token de administrador en header X-ADMIN-TOKEN
    @Operation(summary = "Eliminar usuario (admin)", description = "Elimina un usuario por id. Requiere header X-ADMIN-TOKEN con token de administrador.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader(value = "X-ADMIN-TOKEN", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(403).body("Acceso denegado: token admin inválido");
        }
        // buscar el usuario para obtener su nombre de usuario antes de eliminar
        var maybeUser = repo.findById(id);
        if (maybeUser.isEmpty()) return ResponseEntity.notFound().build();
        var user = maybeUser.get();
        String username = user.getNombreUsuario();
        // eliminar el usuario
        repo.deleteById(id);
        // Intentar limpiar referencias en el foro (comentarios y likes): llamamos con id y con nombre de usuario
        java.util.Map<String, Object> cleanupResult = new java.util.HashMap<>();
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            // limpieza por id (síncrona para devolver resultado al cliente)
            java.net.http.HttpRequest reqId = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8082/api/posts/admin/cleanup-user/" + id))
                    .DELETE()
                    .build();
            var r1 = client.send(reqId, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (r1.statusCode() == 200 && r1.body() != null && !r1.body().isBlank()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> m = mapper.readValue(r1.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>(){});
                    cleanupResult.putAll(m);
                } catch (Exception ex) {
                    cleanupResult.put("idCleanupRaw", r1.body());
                }
            } else {
                cleanupResult.put("idCleanupStatus", r1.statusCode());
            }

            // limpieza por nombre de usuario (string)
            if (username != null && !username.isBlank()) {
                java.net.http.HttpRequest reqName = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8082/api/posts/admin/cleanup-user/" + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8)))
                        .DELETE()
                        .build();
                var r2 = client.send(reqName, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (r2.statusCode() == 200 && r2.body() != null && !r2.body().isBlank()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<String, Object> m2 = mapper.readValue(r2.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>(){});
                        // merge counts
                        m2.forEach((k,v) -> cleanupResult.merge(k, v, (oldV,newV) -> {
                            try {
                                int a = Integer.parseInt(String.valueOf(oldV));
                                int b = Integer.parseInt(String.valueOf(newV));
                                return a + b;
                            } catch (Exception ex) {
                                return newV;
                            }
                        }));
                    } catch (Exception ex) {
                        cleanupResult.put("nameCleanupRaw", r2.body());
                    }
                } else {
                    cleanupResult.put("nameCleanupStatus", r2.statusCode());
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo llamar al foro para limpiar referencias: " + e.getMessage());
            cleanupResult.put("error", e.getMessage());
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "deleted");
        resp.put("cleanup", cleanupResult);
        return ResponseEntity.ok(resp);
    }

    // Listar usuarios (información pública para el panel admin)
    @Operation(summary = "Listar usuarios", description = "Devuelve una lista de usuarios (información pública para panel admin).")
    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listUsers() {
        List<UsuarioDto> users = repo.findAll().stream().map(u -> new UsuarioDto(
                u.getId(), u.getRut(), u.getNombre(), u.getFechaNac(), u.getCorreo(), u.getNombreUsuario(), u.isAdmin(),
                u.getRoleEntity() != null ? u.getRoleEntity().getName() : null,
                u.getCreatedAt()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Obtener usuario por id (público, usado por UI para comprobar rol)
    @Operation(summary = "Obtener usuario por id", description = "Devuelve la información pública del usuario indicado por id.")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> getUserById(@PathVariable Long id) {
        return repo.findById(id).map(u -> new UsuarioDto(
                u.getId(), u.getRut(), u.getNombre(), u.getFechaNac(), u.getCorreo(), u.getNombreUsuario(), u.isAdmin(),
                u.getRoleEntity() != null ? u.getRoleEntity().getName() : null,
                u.getCreatedAt()
        )).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Obtener el usuario 'actual' por id pasado como query param. Esto permite
    // que el frontend consulte /users/me?id=123 sin exponer la lista completa.
    @Operation(summary = "Usuario actual (por id)", description = "Devuelve el usuario 'actual' cuando se consulta con ?id=123")
    @GetMapping("/me")
    public ResponseEntity<UsuarioDto> getCurrentUser(@RequestParam(value = "id", required = false) Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        return repo.findById(id).map(u -> new UsuarioDto(
            u.getId(), u.getRut(), u.getNombre(), u.getFechaNac(), u.getCorreo(), u.getNombreUsuario(), u.isAdmin(),
            u.getRoleEntity() != null ? u.getRoleEntity().getName() : null,
            u.getCreatedAt()
        )).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Actualizar flag isAdmin de un usuario (requiere X-ADMIN-TOKEN)
    @Operation(summary = "Actualizar rol admin", description = "Actualiza el flag isAdmin de un usuario. Requiere X-ADMIN-TOKEN.")
    @PutMapping("/{id}/admin")
    public ResponseEntity<?> setAdmin(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body,
                                      @RequestHeader(value = "X-ADMIN-TOKEN", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(403).body("Acceso denegado: token admin inválido");
        }
        return repo.findById(id).map(u -> {
            boolean next = false;
            if (body != null && body.containsKey("isAdmin")) {
                Object v = body.get("isAdmin");
                if (v instanceof Boolean) next = (Boolean) v;
                else if (v instanceof String) next = Boolean.parseBoolean((String) v);
            } else {
                // Si no viene body, alternar valor
                next = !u.isAdmin();
            }
            u.setAdmin(next);
            // actualizar la relación de rol (ROLE_ADMIN / ROLE_USER)
            String desired = next ? "ROLE_ADMIN" : "ROLE_USER";
            var maybeRole = roleRepo.findByName(desired);
            if (maybeRole.isPresent()) {
                u.setRoleEntity(maybeRole.get());
            } else {
                // intentar crear el rol si no existe
                Rol r = Rol.builder().name(desired).build();
                roleRepo.save(r);
                u.setRoleEntity(r);
            }
            repo.save(u);
            // return the updated user DTO so frontend can sync immediately
            var dto = new microservicio.usuarios.dto.UsuarioDto(
                    u.getId(), u.getRut(), u.getNombre(), u.getFechaNac(), u.getCorreo(), u.getNombreUsuario(), u.isAdmin(),
                    u.getRoleEntity() != null ? u.getRoleEntity().getName() : null, u.getCreatedAt()
            );
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }
}
