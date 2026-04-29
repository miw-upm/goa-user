package es.upm.api.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AddressEncryptionService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String PREFIX = "enc::";

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom;

    public AddressEncryptionService(@Value("${app.crypto.address-key:goa-user-address-key}") String passphrase) {
        this.keySpec = new SecretKeySpec(this.sha256(passphrase), "AES");
        this.secureRandom = new SecureRandom();
    }

    public String encrypt(String value) {
        if (!StringUtils.hasText(value) || this.isEncrypted(value)) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            this.secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            String ivPart = Base64.getEncoder().encodeToString(iv);
            String payloadPart = Base64.getEncoder().encodeToString(cipherText);
            return PREFIX + ivPart + ":" + payloadPart;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot encrypt address", e);
        }
    }

    public String decrypt(String value) {
        if (!StringUtils.hasText(value) || !this.isEncrypted(value)) {
            return value;
        }
        try {
            String payload = value.substring(PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("Invalid encrypted address format");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, this.keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot decrypt address", e);
        }
    }

    private boolean isEncrypted(String value) {
        return value.startsWith(PREFIX);
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot initialize address encryption key", e);
        }
    }
}
