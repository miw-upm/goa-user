package es.upm.api.configurations;

import es.upm.api.data.entities.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class CurrentUser {
    public boolean isCustomer() {
        return getAuthorities().anyMatch(a -> a.equals(Role.CUSTOMER.springSecurityAuthority()));
    }

    public String mobile() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Stream<String> getAuthorities() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority);
    }

    public Role getRole() {
        return this.getAuthorities()
                .findFirst()
                .map(Role::from)
                .orElse(Role.ANONYMOUS);
    }
}
