package microservicio.usuarios.repository;

import microservicio.usuarios.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {
    // Repositorio JPA para Usuarios. Métodos usados por AuthService.
    Optional<Usuarios> findByNombreUsuario(String nombreUsuario);
    Optional<Usuarios> findByCorreo(String correo);
    Optional<Usuarios> findByRut(String rut);
    Optional<Usuarios> findByNombreUsuarioOrCorreo(String nombreUsuario, String correo);

    boolean existsByRut(String rut);
    boolean existsByCorreo(String correo);
    boolean existsByNombreUsuario(String nombreUsuario);
}
