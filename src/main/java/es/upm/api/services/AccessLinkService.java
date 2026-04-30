package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.User;
import es.upm.api.services.criteria.AccessLinkFindCriteria;
import es.upm.miw.base64url.Base64UrlGenerator;
import es.upm.miw.exception.NotFoundException;
import es.upm.miw.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static es.upm.api.data.entities.Role.CUSTOMER;

@RequiredArgsConstructor
@Service
public class AccessLinkService {
    public static final int TOKEN_DURATION_DAYS = 7;
    public static final int TOKEN_USAGE_LIMIT = 7;

    private final UserRepository userRepository;
    private final AccessLinkRepository accessLinkRepository;
    private final PasswordEncoder passwordEncoder;


    public AccessLinkCreationResult create(String mobile, String scope, UUID documentId) {
        User user = this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("User not found: " + mobile));
        String token = Base64UrlGenerator.token();
        AccessLink accessLink = AccessLink.builder()
                .id(UUID.randomUUID())
                .urlId(Base64UrlGenerator.encode())
                .user(user)
                .tokenHash(this.passwordEncoder.encode(token))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(TOKEN_DURATION_DAYS))
                .remainingUses(TOKEN_USAGE_LIMIT)
                .scope(scope)
                .documentId(documentId).build();
        AccessLink saved = this.accessLinkRepository.save(accessLink);
        return new AccessLinkCreationResult(saved, token);

    }

    public AccessLink read(UUID id) {
        return this.accessLinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public void delete(UUID id) {
        accessLinkRepository.deleteById(id);
    }

    public Stream<AccessLink> find(AccessLinkFindCriteria criteria) {
        if (criteria.all()) {
            return this.accessLinkRepository.findAll().stream();
        }
        List<UUID> ids = criteria.getClient() != null
                ? this.userRepository.findCustomersByText(criteria.getClient(), List.of(CUSTOMER)).stream().map(User::getId).toList()
                : List.of();
        return Boolean.FALSE.equals(criteria.getExpired())
                ? this.accessLinkRepository.findActiveUsedByUserIdsAndScope(ids, criteria.getScope()).stream()
                : this.accessLinkRepository.findExpiredUnusedByUserIdsAndScope(ids, criteria.getScope()).stream();
    }

    public AccessLink consumeToken(String scope, String urlId, String token) {
        AccessLink accessLink = this.accessLinkRepository.findByUrlId(urlId).orElseThrow(
                () -> new UnauthorizedException("Unauthorized. Access Link Not Found"));
        if (!passwordEncoder.matches(token, accessLink.getTokenHash())) {
            throw new UnauthorizedException("Unauthorized. Token Invalid");
        }
        accessLink.use(scope);
        return this.accessLinkRepository.save(accessLink);
    }

}
