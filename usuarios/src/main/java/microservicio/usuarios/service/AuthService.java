package microservicio.usuarios.service;

import microservicio.usuarios.dto.AuthResponse;
import microservicio.usuarios.dto.LoginRequest;
import microservicio.usuarios.dto.RegisterRequest;
import microservicio.usuarios.model.Rol;
import microservicio.usuarios.model.Usuarios;
import microservicio.usuarios.repository.UsuariosRepository;
import microservicio.usuarios.repository.RoleRepository;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    // Servicio de autenticación y registro.
    // - register: valida unicidad, asigna rol admin si corresponde y guarda
    //   la contraseña encriptada.
    // - login: valida credenciales y devuelve JWT.

    private final UsuariosRepository repo;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepo;

    // El token de administrador se lee desde application.properties
    @org.springframework.beans.factory.annotation.Value("${app.admin.token:}")
    private String adminToken;

    public AuthService(UsuariosRepository repo, PasswordEncoder encoder, RoleRepository roleRepo) {
        this.repo = repo;
        this.encoder = encoder;
        this.roleRepo = roleRepo;
    }

    public String register(RegisterRequest req) {

        if (repo.existsByRut(req.getRut())) return "RUT ya registrado";
        if (repo.existsByCorreo(req.getCorreo())) return "Correo ya registrado";
        if (repo.existsByNombreUsuario(req.getNombreUsuario())) return "Usuario ya registrado";

        boolean isAdminFlag = false;
        final String roleName;
        if (req.getCorreo().toLowerCase().endsWith("@asfaltofashion.cl")) {
            if (!adminToken.equals(req.getAdminToken()))
                return "Token de administrador inválido";
            roleName = "ROLE_ADMIN";
            isAdminFlag = true;
        } else {
            roleName = "ROLE_USER";
        }

    // find or create role entity
    Rol roleEntity = roleRepo.findByName(roleName).orElseGet(() -> roleRepo.save(Rol.builder().name(roleName).build()));

    Usuarios user = Usuarios.builder()
                .rut(req.getRut())
                .nombre(req.getNombre())
                .fechaNac(req.getFechaNac())
                .correo(req.getCorreo())
                .nombreUsuario(req.getNombreUsuario())
                .password(encoder.encode(req.getPassword()))
        .roleEntity(roleEntity)
        .isAdmin(isAdminFlag)
                .build();

        repo.save(user);
        return "OK";
    }

    public AuthResponse login(LoginRequest req) {
        Usuarios user = repo.findByNombreUsuarioOrCorreo(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Contraseña incorrecta");

        // Generador simple de token: UUID. No expira.
        String token = UUID.randomUUID().toString();
        // Devolver también correo, fecha de nacimiento e id para que el frontend tenga
        // datos completos del usuario al iniciar sesión.
        return new AuthResponse(token, user.getNombreUsuario(), user.isAdmin(), user.getRoleEntity() != null ? user.getRoleEntity().getName() : null, user.getCreatedAt(), user.getCorreo(), user.getFechaNac(), user.getId());
    }
}
