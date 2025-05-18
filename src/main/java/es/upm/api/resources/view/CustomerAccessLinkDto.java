package es.upm.api.resources.view;

import es.upm.api.data.entities.AccessLink;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerAccessLinkDto {
    private String value;

    public CustomerAccessLinkDto(AccessLink accessLink) {
        this.value = "/" + accessLink.getUser().getMobile() + "/" + accessLink.getId();
    }
}
