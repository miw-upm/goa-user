package es.upm.api.configurations;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.exceptions.BadCredentialsException;
import es.upm.api.services.exceptions.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {  // Generate tokens OAuth2
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Properties oAuth2Properties;
    private final UserRepository userRepository;

    @Autowired
    public AuthorizationServerConfig(PasswordEncoder passwordEncoder, OAuth2Properties oAuth2Properties, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.oAuth2Properties = oAuth2Properties;
        this.userRepository = userRepository;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer
                authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        RequestMatcher endpoints = authorizationServerConfigurer.getEndpointsMatcher();
        return http
                .securityMatcher(endpoints)
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpoints))
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer.oidc(Customizer.withDefaults())    // Enable OpenID Connect 1.0
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/actuator").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(1))
                .build();

        RegisteredClient spaClient =
                RegisteredClient.withId("spa-client")
                        .clientId(oAuth2Properties.getSpaClientId())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri(this.oAuth2Properties.getSpaLoginRedirectUri())
                        .scopes(scopes -> scopes.addAll(OAuth2Scope.allJwtClaimValues()))
                        .tokenSettings(tokenSettings)
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .requireProofKey(true)
                                .build())
                        .postLogoutRedirectUri(this.oAuth2Properties.getSpaLogoutRedirectUri())
                        .build();

        RegisteredClient openApiClient =
                RegisteredClient.withId("open-api-client")
                        .clientId(this.oAuth2Properties.getOpenApiClientId())
                        .clientSecret(passwordEncoder.encode(this.oAuth2Properties.getOpenApiClientSecret()))
                        .clientAuthenticationMethods(methods -> methods.addAll(Set.of(
                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                                ClientAuthenticationMethod.CLIENT_SECRET_POST
                        )))
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUris(uris -> uris.addAll(this.oAuth2Properties.getOpenApiRedirectUris()))
                        .scopes(scopes -> scopes.addAll(OAuth2Scope.allJwtClaimValues()))
                        .tokenSettings(tokenSettings)
                        .build();

        RegisteredClient apiClient =
                RegisteredClient.withId("api-client")
                        .clientId(this.oAuth2Properties.getApiClientId())
                        .clientSecret(passwordEncoder.encode(this.oAuth2Properties.getApiClientSecret()))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope(OAuth2Scope.PROFILE.jwtClaimValue())
                        .tokenSettings(tokenSettings)
                        .build();

        return new InMemoryRegisteredClientRepository(openApiClient, apiClient, spaClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa(); // Genera el par de claves
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private RSAKey generateRsa() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("goa-jwt")
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(this.oAuth2Properties.getIssuerUri()) //Emisor
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizerByRolesAndName() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Set<String> roles = new HashSet<>();
                if (context.getPrincipal() != null
                        && context.getPrincipal().getAuthorities() != null
                        && !context.getPrincipal().getAuthorities().isEmpty()) {
                    roles.addAll(
                            context.getPrincipal().getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .map(Role::from)
                                    .map(Role::jwtClaimValue)
                                    .collect(Collectors.toSet())
                    ); //OAuth2Scope of user
                    String mobile = context.getPrincipal().getName();
                    context.getClaims().claim("name",
                            this.userRepository.findByMobile(mobile)
                                    .orElseThrow(() -> new NotFoundException("Mobile not found: " + mobile))
                                    .getFirstName());
                } else if (context.getAuthorizationGrant() instanceof OAuth2ClientCredentialsAuthenticationToken clientCredentialsToken) {
                    String roleParam = (String) clientCredentialsToken.getAdditionalParameters().get("role");
                    if (roleParam == null || roleParam.isBlank()) {
                        throw new BadCredentialsException("Invalid token: missing role");
                    }
                    roles.add(Role.from(roleParam).jwtClaimValue());
                }
                context.getClaims().claim("roles", String.join(" ", roles));
            }
        };
    }

}
