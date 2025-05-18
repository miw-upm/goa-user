package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.data.entities.UUIDBase64;
import es.upm.api.data.entities.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
public class AccessLinkService {
    public static final int TOKEN_DURATION_DAYS = 5;
    public static final int TOKEN_USAGE_LIMIT = 4;

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

    public Stream<AccessLink> findAll() {
        return this.accessLinkRepository.findAll().stream();
    }

    @Transactional
    public void deleteById(String idSuffix) {
        this.accessLinkRepository.deleteByIdSuffix(idSuffix);
    }
}
