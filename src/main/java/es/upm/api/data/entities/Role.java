package es.upm.api.data.entities;

import java.util.Arrays;
import java.util.List;

public enum Role {
    ADMIN, MANAGER, OPERATOR, CUSTOMER, URL_TOKEN, ANONYMOUS, AUTHENTICATED;

    public static final String PREFIX = "ROLE_";

    public static List<String> allJwtClaimValues() {
        return Arrays.stream(Role.values())
                .map(Role::jwtClaimValue)
                .toList();
    }

    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Missing role");
        }
        String normalized = value.trim().replace(PREFIX, "").toUpperCase();
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid role: " + value);
        }
    }

    public String springSecurityAuthority() {
        return PREFIX + this.jwtClaimValue();
    }

    public String jwtClaimValue() {
        return this.name().toLowerCase();
    }

}
