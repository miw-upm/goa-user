package es.upm.api.data.entities;

import es.upm.miw.exception.ForbiddenException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AccessLink {
    @Id
    private String id;
    @DBRef
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private UUID document;

    public void use() {
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("Expired token");
        }
        if (this.remainingUses <= 0) {
            throw new ForbiddenException("Used token");
        }
        this.remainingUses--;
    }
}

