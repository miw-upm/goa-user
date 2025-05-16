package es.upm.api.resources.view;

import es.upm.api.data.entities.AccessLink;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessLinkDto {
    private String accessLink;

    public AccessLinkDto(AccessLink accessLink) {
        this.accessLink = accessLink.getUser().getMobile() + "/" + accessLink.getId();
    }
}
