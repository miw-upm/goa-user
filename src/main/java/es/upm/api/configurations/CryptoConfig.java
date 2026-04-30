package es.upm.api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class CryptoConfig {

    @Bean
    public TextEncryptor textEncryptor(
            @Value("${app.crypto.password}") String password,
            @Value("${app.crypto.salt}") String salt
    ) {
        return Encryptors.delux(password, salt);
    }
}