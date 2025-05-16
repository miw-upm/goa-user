package es.upm.api.resources.view;

import es.upm.api.data.entities.AccessLink;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessLinkDto {
    private String value;

    public AccessLinkDto(AccessLink accessLink) {
        this.value = "/" + accessLink.getUser().getMobile() + "/" + accessLink.getId();
    }
}
