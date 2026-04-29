package es.upm.api.resources.dtos;

import es.upm.api.data.entities.AccessLink;
import es.upm.api.services.AccessLinkCreationResult;
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
public class AccessLinkResponseDto {
    private UUID id;
    private String urlId;
    private String token;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private UUID documentId;

    public AccessLinkResponseDto(AccessLink accessLink) {
        BeanUtils.copyProperties(accessLink, this);
        this.setFullName(accessLink.getUser().fullName());
    }

    public AccessLinkResponseDto(AccessLinkCreationResult creationResult) {
        this(creationResult.accessLink());
        this.setToken(creationResult.token());
    }

    public AccessLinkResponseDto ofSummary() {
        return AccessLinkResponseDto.builder()
                .id(this.getId())
                .fullName(this.getFullName())
                .lastUsedAt(this.getLastUsedAt())
                .expiresAt(this.getExpiresAt())
                .scope(this.getScope())
                .urlId(this.getUrlId())
                .build();
    }


}

