package microservicio.usuarios.bootstrap;

import microservicio.usuarios.model.Rol;
import microservicio.usuarios.model.Usuarios;
import microservicio.usuarios.repository.UsuariosRepository;
import microservicio.usuarios.repository.RoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class AdminDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataLoader.class);

    @Bean
    CommandLineRunner initAdmin(UsuariosRepository repo, PasswordEncoder encoder, RoleRepository roleRepo, JdbcTemplate jdbc) {
        return args -> {
            try {
                // ensure ROLE_USER and ROLE_ADMIN exist
                Rol roleUser = roleRepo.findByName("ROLE_USER").orElseGet(() -> roleRepo.save(Rol.builder().name("ROLE_USER").build()));
                Rol roleAdmin = roleRepo.findByName("ROLE_ADMIN").orElseGet(() -> roleRepo.save(Rol.builder().name("ROLE_ADMIN").build()));

                // migrate existing 'rol' column values into the new roleEntity (role_id FK)
                try {
                    jdbc.query("SELECT id, rol FROM usuarios", rs -> {
                        long id = rs.getLong("id");
                        String rolName = rs.getString("rol");
                        if (rolName == null) rolName = "ROLE_USER";
                        Rol desired = "ROLE_ADMIN".equalsIgnoreCase(rolName) ? roleAdmin : roleUser;
                        repo.findById(id).ifPresent(u -> {
                            if (u.getRoleEntity() == null || !desired.getName().equals(u.getRoleEntity().getName())) {
                                u.setRoleEntity(desired);
                                repo.save(u);
                            }
                        });
                        return null;
                    });
                } catch (Exception ex) {
                    // If table/column doesn't exist (fresh schema) ignore
                    logger.debug("No se pudo migrar columna 'rol' (si no existe está bien): {}", ex.getMessage());
                }

                if (repo.findByNombreUsuario("admin").isEmpty()) {
                    Usuarios admin = new Usuarios();
                    admin.setNombreUsuario("admin");
                    admin.setCorreo("admin@asfaltofashion.cl");
                    admin.setNombre("Admin");
                    admin.setRut("00000000-0");
                    admin.setFechaNac("1985-01-01");
                    admin.setPassword(encoder.encode("admin"));
                    admin.setAdmin(true);
                    admin.setRoleEntity(roleAdmin);
                    repo.save(admin);
                    logger.info("Admin creado: admin / admin");
                }
            } catch (Exception e) {
                logger.error("No se pudo crear admin: {}", e.getMessage());
            }
        };
    }
}

