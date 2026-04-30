package es.upm.api.configurations;

import es.upm.api.resources.AccessLinksResource;
import es.upm.api.resources.DataProcessingConsentResource;
import es.upm.api.resources.SystemResource;
import es.upm.api.resources.UserResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {
    public static final String CLAIM_NAME = "roles";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AWS_CLAIM_NAME = "cognito:groups";

    @Bean
    @Order(1)
    public SecurityFilterChain systemPublic(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(SystemResource.SYSTEM + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiJwt(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, UserResource.USERS).permitAll()
                        .requestMatchers(HttpMethod.GET, UserResource.USERS + UserResource.PROVINCES).permitAll()
                        .requestMatchers(HttpMethod.GET, UserResource.USERS + "/*/*/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, UserResource.USERS + "/*/*/*").permitAll()
                        .anyRequest().authenticated()
                )
                .securityMatcher(
                        UserResource.USERS + "/**",
                        AccessLinksResource.ACCESS_LINKS + "/**",
                        DataProcessingConsentResource.CONSENTS + "/**"
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);
        grantedAuthoritiesConverter.setAuthoritiesClaimName(CLAIM_NAME);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            if (jwt.getClaim(CLAIM_NAME) != null) { // standard Auth2
                return grantedAuthoritiesConverter.convert(jwt);
            } else {
                return Optional.ofNullable(jwt.getClaimAsStringList(AWS_CLAIM_NAME))// AWS cognito: group as role
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group))
                        .collect(Collectors.toList());
            }
        });
        return jwtAuthenticationConverter;
    }
}
