package microservicio.usuarios.service;

import microservicio.usuarios.dto.AuthResponse;
import microservicio.usuarios.dto.LoginRequest;
import microservicio.usuarios.dto.RegisterRequest;
import microservicio.usuarios.model.Rol;
import microservicio.usuarios.model.Usuarios;
import microservicio.usuarios.repository.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UsuariosRepository repo;

    @Mock
    private PasswordEncoder encoder;
    
    @Mock
    private microservicio.usuarios.repository.RoleRepository roleRepo;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_nuevoUsuario_devuelveOK() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("1-9");
        req.setCorreo("u@x.com");
        req.setNombreUsuario("usuario1");
        req.setPassword("pass");
        req.setNombre("Nombre");

        when(repo.existsByRut("1-9")).thenReturn(false);
        when(repo.existsByCorreo("u@x.com")).thenReturn(false);
        when(repo.existsByNombreUsuario("usuario1")).thenReturn(false);
        when(encoder.encode("pass")).thenReturn("encoded");
        when(repo.save(any(Usuarios.class))).thenAnswer(i -> i.getArgument(0));

        String res = authService.register(req);
        assertEquals("OK", res);
    }

    @Test
    void register_rutExistente_devuelveError() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("1-9");
        when(repo.existsByRut("1-9")).thenReturn(true);
        String res = authService.register(req);
        assertTrue(res.contains("RUT"));
    }

    @Test
    void login_ok_devuelveAuthResponse() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("juan");
        req.setPassword("pass");

        Usuarios u = Usuarios.builder().id(1L).nombreUsuario("juan").password("encoded").correo("j@x").build();
        when(repo.findByNombreUsuarioOrCorreo("juan","juan")).thenReturn(Optional.of(u));
        when(encoder.matches("pass","encoded")).thenReturn(true);

        AuthResponse r = authService.login(req);
        assertNotNull(r.getToken());
        assertEquals("juan", r.getUsername());
    }

    @Test
    void login_passwordIncorrecta_lanzaExcepcion() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("juan");
        req.setPassword("bad");
        Usuarios u = Usuarios.builder().id(1L).nombreUsuario("juan").password("encoded").build();
        when(repo.findByNombreUsuarioOrCorreo("juan","juan")).thenReturn(Optional.of(u));
        when(encoder.matches("bad","encoded")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }
}
