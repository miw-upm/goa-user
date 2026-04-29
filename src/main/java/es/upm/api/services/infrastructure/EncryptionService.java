package es.upm.api.services.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final String PREFIX = "enc::";
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    private final TextEncryptor textEncryptor;

    public String encrypt(String value) {
        if (!StringUtils.hasText(value) || this.isPrefixed(value)) {
            return value;
        }
        return PREFIX + this.textEncryptor.encrypt(value);
    }

    public String decrypt(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (this.isPrefixed(value)) {
            return this.decryptCipher(value.substring(PREFIX.length()));
        }
        if (this.isLegacyCipherText(value)) {
            try {
                return this.textEncryptor.decrypt(value);
            } catch (IllegalArgumentException e) {
                return value;
            }
        }
        return value;
    }

    private String decryptCipher(String cipherText) {
        try {
            return this.textEncryptor.decrypt(cipherText);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Cannot decrypt value", e);
        }
    }

    private boolean isPrefixed(String value) {
        return value.startsWith(PREFIX);
    }

    private boolean isLegacyCipherText(String value) {
        return value.length() % 2 == 0 && value.length() >= 32 && HEX_PATTERN.matcher(value).matches();
    }
}
