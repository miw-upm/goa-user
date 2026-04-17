package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.data.entities.User;
import es.upm.miw.exception.NotFoundException;
import es.upm.miw.uuid.UUIDBase64;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class AccessLinkService {
    public static final int TOKEN_DURATION_DAYS = 7;
    public static final int TOKEN_USAGE_LIMIT = 10;

    private final UserService userService;
    private final AccessLinkRepository accessLinkRepository;

    public AccessLinkService(UserService userService, AccessLinkRepository accessLinkRepository) {
        this.userService = userService;
        this.accessLinkRepository = accessLinkRepository;
    }

    public AccessLink create(@Valid CreationAccessLink creationAccessLink) {
        User user = this.userService.readByMobile(creationAccessLink.getMobile());
        AccessLink accessLink = AccessLink.builder().id(UUIDBase64.URL.encode()).user(user)
                .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(TOKEN_DURATION_DAYS))
                .remainingUses(TOKEN_USAGE_LIMIT).scope(creationAccessLink.getScope()).build();
        return this.accessLinkRepository.save(accessLink);
    }

    public Stream<AccessLink> findNullSafe(AccessLinkFindCriteria criteria) {
        if (criteria.all()) {
            return this.accessLinkRepository.findAll().stream();
        }

        List<UUID> ids = criteria.getMobile() != null
                ? this.userService.findIdsByMobileContaining(criteria.getMobile()).toList()
                : List.of();

        return Boolean.FALSE.equals(criteria.getExpired())
                ? this.accessLinkRepository.searchActiveUsedByUserIdsAndScope(ids, criteria.getScope()).stream()
                : this.accessLinkRepository.searchExpiredUnusedByUserIdsAndScope(ids, criteria.getScope()).stream();
    }

    public void deleteById(String idSuffix) {
        List<AccessLink> links = accessLinkRepository.findByIdSuffix(idSuffix);
        accessLinkRepository.deleteAll(links);
    }

    public AccessLink read(String idSuffix) {
        return this.accessLinkRepository.read(idSuffix).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + idSuffix));
    }
}
