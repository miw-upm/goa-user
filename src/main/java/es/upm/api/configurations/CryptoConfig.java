package es.upm.api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class CryptoConfig {

    @Bean("textEncryptor")
    public TextEncryptor textEncryptor(
            @Value("${app.crypto.salt}") String salt,
            @Value("${app.crypto.password}") String password
    ) {
        return Encryptors.delux(password, salt);
    }

    @Bean("legacyTextEncryptor")
    @ConditionalOnProperty(prefix = "app.crypto", name = "previous-password")
    public TextEncryptor legacyTextEncryptor(
            @Value("${app.crypto.previous-salt}") String salt,
            @Value("${app.crypto.previous-password}") String password
    ) {
        return Encryptors.delux(password, salt);
    }
}