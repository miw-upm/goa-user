package es.upm.api.data.entities;

import es.upm.api.services.exceptions.ForbiddenException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AccessLink {
    @Id
    private String id;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;

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

