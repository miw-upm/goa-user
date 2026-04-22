package es.upm.api.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.oauth2")
@Data
public class OAuth2Properties {
    private String tokenUri;
    private String openApiClientId;
    private String openApiClientSecret;
    private List<String> openApiRedirectUris = new ArrayList<>();
    private String spaClientId;
    private String spaLoginRedirectUri;
    private String spaLogoutRedirectUri;
    private String apiClientId;
    private String apiClientSecret;
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String keyStoreBase64;
        private String keyAlias;
        private String keyStorePassword;
        private String keyPassword;
        private String keyId;
    }
}

