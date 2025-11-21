package microservicio.foro.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSummary {
    private Long id;
    private String externalId;
    private String title;
    private String image;
    private String category;
    private String createdAt;
}
