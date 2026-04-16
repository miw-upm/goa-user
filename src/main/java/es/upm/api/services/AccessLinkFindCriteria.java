package es.upm.api.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkFindCriteria {
    private Boolean expired;
    private String mobile;
    private String scope;

    public boolean all() {
        return mobile == null && scope == null && expired == false;
    }
}
