package microservicio.usuarios.controller;

import microservicio.usuarios.repository.UsuariosRepository;
import microservicio.usuarios.model.Rol;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final UsuariosRepository usuariosRepo;

    public RoleController(UsuariosRepository usuariosRepo) {
        this.usuariosRepo = usuariosRepo;
    }

    // Devuelve lista de usuarios con su rol (join)
    @GetMapping("/users")
    public List<RoleUserDto> listUsersWithRoles() {
        return usuariosRepo.findAll().stream().map(u -> {
            String roleName = null;
            Rol r = u.getRoleEntity();
            if (r != null) roleName = r.getName();
            return new RoleUserDto(u.getId(), u.getNombreUsuario(), roleName);
        }).collect(Collectors.toList());
    }

    public static class RoleUserDto {
        public Long id;
        public String nombreUsuario;
        public String rol;

        public RoleUserDto(Long id, String nombreUsuario, String rol) {
            this.id = id;
            this.nombreUsuario = nombreUsuario;
            this.rol = rol;
        }
    }
}
