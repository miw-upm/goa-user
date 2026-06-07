package es.upm.api.services.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFindCriteria {
    private Boolean active;
    private String customer;
    private Boolean billable;

    public boolean all() {
        return active == null && (customer == null || customer.isBlank()) && billable == null;
    }
}
