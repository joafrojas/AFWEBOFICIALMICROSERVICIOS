package microservicio.foro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    @Column(columnDefinition = "LONGTEXT")
    private String image;

    @Column(name = "author_id", nullable = false)
    private String authorId; 

    @Column(name = "category")
    private String category;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "created_at")
    private String createdAt;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user")
    private List<String> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now().toString();
    }
}
