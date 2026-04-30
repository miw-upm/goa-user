package es.upm.api.services.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFindCriteria {
    private String mobile;
    private String firstName;
    private String familyName;
    private String customer;
    private boolean projection = false;

    public boolean all() {
        return mobile == null && firstName == null && familyName == null && customer == null;
    }
}
