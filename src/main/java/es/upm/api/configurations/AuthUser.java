package es.upm.api.configurations;

import es.upm.api.data.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthUser implements UserDetails {
    private final boolean enabled;
    private final String username;
    private final String password;
    @Getter
    private final String firstName;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(User user) {
        this.enabled = Boolean.TRUE.equals(user.getActive());
        this.username = user.getMobile();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().springSecurityAuthority()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}

