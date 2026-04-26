package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.AccessLinkDto;
import es.upm.api.services.criteria.AccessLinkFindCriteria;
import es.upm.miw.exception.NotFoundException;
import es.upm.miw.exception.UnauthorizedException;
import es.upm.miw.uuid.UUIDBase64;
import lombok.RequiredArgsConstructor;
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
    public static final int TOKEN_USAGE_LIMIT = 10;

    private final UserRepository userRepository;
    private final AccessLinkRepository accessLinkRepository;

    public AccessLink create(String mobile, String scope, UUID document) {
        User user = this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("User not found: " + mobile));
        AccessLink accessLink = AccessLink.builder().id(UUIDBase64.URL.encode()).user(user)
                .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(TOKEN_DURATION_DAYS))
                .remainingUses(TOKEN_USAGE_LIMIT).scope(scope).document(document).build();
        return this.accessLinkRepository.save(accessLink);
    }

    public AccessLink read(String id) {
        return this.accessLinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public void deleteById(String id) {
        accessLinkRepository.deleteById(id);
    }

    public Stream<AccessLink> find(AccessLinkFindCriteria criteria) {
        if (criteria.all()) {
            return this.accessLinkRepository.findAll().stream();
        }
        List<UUID> ids = criteria.getClient() != null
                ? this.userRepository.findByAll(criteria.getClient(), List.of(CUSTOMER)).stream().map(User::getId).toList()
                : List.of();
        return Boolean.FALSE.equals(criteria.getExpired())
                ? this.accessLinkRepository.findActiveUsedByUserIdsAndScope(ids, criteria.getScope()).stream()
                : this.accessLinkRepository.findExpiredUnusedByUserIdsAndScope(ids, criteria.getScope()).stream();
    }

    public AccessLinkDto use(String id, String mobile, String scope) {
        AccessLink accessLink = this.accessLinkRepository.findById(id).orElseThrow(
                () -> new UnauthorizedException("Unauthorized. Access Link Not Founded"));
        accessLink.use(mobile, scope);
        this.accessLinkRepository.save(accessLink);
        return new AccessLinkDto(accessLink);
    }
}
