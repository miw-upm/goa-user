package es.upm.api.resources.dtos;

import es.upm.api.data.entities.AccessLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkDto {
    private UUID id;
    private String fullName;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private UUID documentId;

    public AccessLinkDto(AccessLink accessLink) {
        BeanUtils.copyProperties(accessLink, this);
        this.setFullName(accessLink.getUser().fullName());
    }

}

