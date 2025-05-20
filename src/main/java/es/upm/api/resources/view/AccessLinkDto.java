package es.upm.api.resources.view;

import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkDto {
    public static final String MASK = "***";
    public static final int CODE_SIZE = 8;
    private String id;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer remainingUses;
    private String scope;
    private String link;

    public AccessLinkDto(AccessLink accessLink) {
        BeanUtils.copyProperties(accessLink, this);
        this.link = String.format("/%s/%s", accessLink.getUser().getMobile(), accessLink.getId());
        this.id = MASK + this.id.substring(this.id.length() - CODE_SIZE);
    }

    public static AccessLinkDto ofSummary(AccessLinkDto accessLinkDto) {
        return AccessLinkDto.builder()
                .id(accessLinkDto.id)
                .user(User.builder().mobile(accessLinkDto.getUser().getMobile()).build())
                .expiresAt(accessLinkDto.expiresAt)
                .remainingUses(accessLinkDto.remainingUses)
                .scope(accessLinkDto.scope)
                .build();
    }

    public static AccessLinkDto ofLink(AccessLinkDto accessLinkDto) {
        return AccessLinkDto.builder()
                .link(accessLinkDto.link)
                .build();
    }

    public static String cleanId(String id) {
        return id.substring(MASK.length());
    }

}

