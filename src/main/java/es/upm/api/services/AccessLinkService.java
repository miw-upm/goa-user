package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.data.entities.UUIDBase64;
import es.upm.api.data.entities.User;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.services.exceptions.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccessLinkService {

    UserService userService;
    AccessLinkRepository accessLinkRepository;

    public AccessLinkService(UserService userService, AccessLinkRepository accessLinkRepository) {
        this.userService = userService;
        this.accessLinkRepository = accessLinkRepository;
    }

    public AccessLink create(@Valid CreationAccessLink creationAccessLink) {
        User user = this.userService.readByMobile(creationAccessLink.getMobile());
        AccessLink accessLink = AccessLink.builder().id(UUIDBase64.URL.encode()).user(user)
                .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(5))
                .used(4).purpose(creationAccessLink.getPurpose()).build();
        return this.accessLinkRepository.save(accessLink);
    }

    public String validateAndUse(String mobile, String token) {
        this.userService.readByMobile(mobile);
        AccessLink accessLink = this.accessLinkRepository.findById(token)
                .orElseThrow(() -> new NotFoundException("The token don't exist: " + token));
        if (!accessLink.getUser().getMobile().equals(mobile)) {
            throw new ForbiddenException("Forbidden token");
        }
        if (accessLink.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ForbiddenException("Expired token");
        }
        if (accessLink.getUsed() <= 0) {
            throw new ForbiddenException("Used token");
        }
        accessLink.setUsed(accessLink.getUsed() - 1);
        this.accessLinkRepository.save(accessLink);
        return accessLink.getPurpose();
    }
}
