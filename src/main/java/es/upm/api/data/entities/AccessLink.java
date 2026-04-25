package es.upm.api.data.entities;

import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
    private String id;
    @DBRef
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private UUID document;

    public void use(String mobile, String requiredScope) {
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Expired token");
        }
        if (this.remainingUses <= 0) {
            throw new UnauthorizedException("Used token");
        }
        if (!Objects.equals(this.user.getMobile(), mobile)) {
            throw new ForbiddenException("Forbidden token. Token is the another mobile");
        }
        if (!Objects.equals(this.scope, requiredScope)) {
            throw new ForbiddenException("Forbidden purpose. The scope does not match the intended use.");
        }
        this.remainingUses--;
        this.lastUsedAt = LocalDateTime.now();
    }
}


