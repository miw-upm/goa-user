package es.upm.api.resources;

import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.resources.view.AccessLinkDto;
import es.upm.api.services.AccessLinkService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AccessLinksResource.ACCESS_LINK)
public class AccessLinksResource {
    public static final String ACCESS_LINK = "/access-link";
    private final AccessLinkService accessLinkService;

    @Autowired
    public AccessLinksResource(AccessLinkService accessLinkService) {
        this.accessLinkService = accessLinkService;
    }

    @PostMapping
    public AccessLinkDto create(@Valid @RequestBody CreationAccessLink creationAccessLink) {
        return new AccessLinkDto(this.accessLinkService.create(creationAccessLink));
    }

}
