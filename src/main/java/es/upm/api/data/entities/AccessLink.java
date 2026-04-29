package es.upm.api.data.entities;

import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AccessLink {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String urlId;
    @DBRef
    private User user;
    private String tokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private UUID documentId;

    public void use(String requiredScope) {
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Expired token");
        }
        if (this.remainingUses <= 0) {
            throw new UnauthorizedException("Used token");
        }
        if (!requiredScope.isBlank() && !Objects.equals(this.scope, requiredScope)) {
            throw new ForbiddenException("Forbidden purpose. The scope does not match the intended use.");
        }
        this.remainingUses--;
        this.lastUsedAt = LocalDateTime.now();
    }
}


