package microservicio.usuarios.controller;

import jakarta.validation.Valid;
import microservicio.usuarios.dto.AuthResponse;
import microservicio.usuarios.dto.LoginRequest;
import microservicio.usuarios.dto.RegisterRequest;
import microservicio.usuarios.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    // Controlador REST para autenticación.
    // - POST /auth/register: registra usuarios (usa @Valid).
    // - POST /auth/login: autentica y devuelve token.
    // CORS: permitido para el frontend de desarrollo en localhost:5173.

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            String result = authService.register(req);
            if (!"OK".equals(result)) return ResponseEntity.badRequest().body(result);
            return ResponseEntity.status(201).body("Registrado: " + req.getNombreUsuario());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error interno");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse res = authService.login(req);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error interno");
        }
    }
}
