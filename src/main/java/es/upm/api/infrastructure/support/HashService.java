package es.upm.api.infrastructure.support;

import es.upm.miw.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Log4j2
public class HashService {

    private static final String BCRYPT_PREFIX = "{bcrypt}";

    private final PasswordEncoder passwordEncoder;

    public String hash(String value) {
        if (!StringUtils.hasText(value) || this.isHashed(value)) {
            return value;
        }
        String hashed = this.passwordEncoder.encode(value);
        if (!hashed.startsWith(BCRYPT_PREFIX)) {
            log.warn("Expected hash prefix '{}'.", BCRYPT_PREFIX);
        }
        return hashed;
    }

    public boolean isHashed(String value) {
        return StringUtils.hasText(value) && value.startsWith(BCRYPT_PREFIX);
    }

    public void matches(String rawToken, String hashToken) {
        if (!this.passwordEncoder.matches(rawToken, hashToken)) {
            throw new UnauthorizedException("Unauthorized. Token Invalid");
        }
    }
}
