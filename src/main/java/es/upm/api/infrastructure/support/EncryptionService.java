package es.upm.api.infrastructure.support;

import es.upm.miw.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final char SEPARATOR = ':';
    public static final String PREFIX_BASE = "enc" + SEPARATOR;
    public static final String PREFIX = PREFIX_BASE + "v1" + SEPARATOR;

    private static final int PREVIEW_CHARS = 12;
    private static final String PREVIEW_TRUNCATION_MARK = "****";

    private final TextEncryptor textEncryptor;

    public String encrypt(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (this.isPrefixed(value)) {
            throw new ConflictException("Value is already encrypted: " + value);
        }
        return PREFIX + this.textEncryptor.encrypt(value);
    }

    public String decrypt(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (!this.isPrefixed(value)) {
            throw new ConflictException("Expected an encrypted value with a valid prefix: " + value);
        }
        String valueWithoutPrefix = value.substring(this.extractAllPrefix(value).length());
        return this.textEncryptor.decrypt(valueWithoutPrefix);
    }

    public boolean isEncrypted(String value) {
        return StringUtils.hasText(value) && this.isPrefixed(value);
    }

    public String extractPreview(String value) {
        String prefix = this.extractAllPrefix(value);
        String cipherText = value.substring(prefix.length());
        String previewChars = cipherText.substring(0, PREVIEW_CHARS);
        return prefix + previewChars + PREVIEW_TRUNCATION_MARK;
    }

    public String extractAllPrefix(String value) {
        if (!value.startsWith(PREFIX_BASE)) {
            throw new ConflictException("Unsupported encryption prefix format: " + value);
        }
        int separatorIndex = value.indexOf(SEPARATOR, PREFIX_BASE.length());
        if (separatorIndex < 0) {
            throw new ConflictException("Malformed encryption prefix - missing separator: " + value);
        }
        return value.substring(0, separatorIndex + 1);
    }

    private boolean isPrefixed(String value) {
        return value.startsWith(PREFIX_BASE);
    }

}