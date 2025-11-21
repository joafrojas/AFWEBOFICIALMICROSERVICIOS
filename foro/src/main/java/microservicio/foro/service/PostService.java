package microservicio.foro.service;

import microservicio.foro.model.Comment;
import microservicio.foro.model.Post;
import microservicio.foro.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository repo;
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(PostRepository repo) {
        this.repo = repo;
    }

    // Listar todas las publicaciones ordenadas por fecha de creación (descendente)
    public List<Post> listarTodas() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // Buscar por id numérico
    public Optional<Post> buscarPorId(Long id) {
        return repo.findById(id);
    }

    // Buscar por externalId 
    public Optional<Post> buscarPorExternalId(String externalId) {
        return repo.findByExternalId(externalId);
    }

    // Buscar por identificador genérico: si es numérico busca por id, si no por externalId
    public Optional<Post> buscarPorIdentificador(String idOrExternal) {
        if (idOrExternal == null) return Optional.empty();
        try {
            Long numeric = Long.parseLong(idOrExternal);
            return buscarPorId(numeric);
        } catch (NumberFormatException nfe) {
            return buscarPorExternalId(idOrExternal);
        }
    }

    // Crear una publicación
    public Post crear(Post p) {
        // Generar externalId si no viene desde el cliente
        if (p.getExternalId() == null || p.getExternalId().isBlank()) {
            String candidate = "p" + System.currentTimeMillis();
            int attempts = 0;
            while (repo.findByExternalId(candidate).isPresent() && attempts < 5) {
                candidate = "p" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
                attempts++;
            }
            p.setExternalId(candidate);
        }
        return repo.save(p);
    }
    

    // Eliminar por id
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    // Agregar comentario a una publicación
    public Comment agregarComentario(Long postId, Comment c) {
        Post p = repo.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));
        p.getComments().add(c);
        repo.save(p);
        return c;
    }

    // Eliminar comentario por id
    public void eliminarComentario(Long postId, Long commentId) {
        Post p = repo.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));
        p.getComments().removeIf(cm -> cm.getId() != null && cm.getId().equals(commentId));
        repo.save(p);
    }

    // Alternar like para un usuario en una publicación
    public Post alternarLike(Long postId, String userId) {
        Post p = repo.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));
        if (p.getLikes().contains(userId)) p.getLikes().remove(userId); else p.getLikes().add(userId);
        return repo.save(p);
    }

    // Remover referencias de un usuario: eliminar comentarios y likes de todas las publicaciones
    public java.util.Map<String, Integer> removerReferenciasUsuario(String userId) {
        List<Post> all = repo.findAll();
        int totalCommentsRemoved = 0;
        int totalLikesRemoved = 0;
        int postsModified = 0;
        for (Post p : all) {
            boolean modified = false;
            // eliminar comentarios cuyo userId coincida
            if (p.getComments() != null) {
                int before = p.getComments().size();
                p.getComments().removeIf(c -> userId != null && userId.equals(c.getUserId()));
                int after = p.getComments().size();
                int removed = before - after;
                if (removed > 0) {
                    totalCommentsRemoved += removed;
                    modified = true;
                }
            }
            // eliminar likes que coincidan con userId
            if (p.getLikes() != null && p.getLikes().contains(userId)) {
                int beforeLikes = p.getLikes().size();
                p.getLikes().removeIf(l -> userId.equals(l));
                int afterLikes = p.getLikes().size();
                int removedLikes = beforeLikes - afterLikes;
                if (removedLikes > 0) {
                    totalLikesRemoved += removedLikes;
                    modified = true;
                }
            }
            if (modified) {
                repo.save(p);
                postsModified++;
                logger.info("Removidas referencias del usuario {} en post {}: commentsRemoved={}, likesRemoved={}", userId, p.getId(), (p.getComments() == null ? 0 : 0), 0);
            }
        }
        java.util.Map<String,Integer> result = new java.util.HashMap<>();
        result.put("commentsRemoved", totalCommentsRemoved);
        result.put("likesRemoved", totalLikesRemoved);
        result.put("postsModified", postsModified);
        logger.info("Cleanup user {}: commentsRemoved={}, likesRemoved={}, postsModified={}", userId, totalCommentsRemoved, totalLikesRemoved, postsModified);
        return result;
    }
}
