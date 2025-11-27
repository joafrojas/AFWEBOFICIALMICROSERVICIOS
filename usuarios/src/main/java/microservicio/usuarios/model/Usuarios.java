package microservicio.usuarios.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "rut"),
        @UniqueConstraint(columnNames = "correo"),
        @UniqueConstraint(columnNames = "nombre_usuario")
})
// Entidad Usuarios: representa la tabla 'usuarios' en la BD.
// Campos principales: rut, nombre, fecha_nacimiento, correo, nombre_usuario, password, rol, is_admin.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rut;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_nacimiento")
    private String fechaNac; 

    @Column(nullable = false)
    private String correo;

    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Rol roleEntity;
    
    @Column(name = "created_at")
    private String createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = java.time.OffsetDateTime.now().toString();
    }
}

