package es.upm.api.resources;

import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.resources.view.AccessLinkDto;
import es.upm.api.services.AccessLinkService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@Log4j2
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AccessLinksResource.ACCESS_LINK)
public class AccessLinksResource {
    public static final String ACCESS_LINK = "/access-link";
    public static final String ID_ID = "/{id}";
    private final AccessLinkService accessLinkService;

    @Autowired
    public AccessLinksResource(AccessLinkService accessLinkService) {
        this.accessLinkService = accessLinkService;
    }

    @PostMapping
    public AccessLinkDto create(@Valid @RequestBody CreationAccessLink creationAccessLink) {
        AccessLinkDto dto = new AccessLinkDto(accessLinkService.create(creationAccessLink));
        return AccessLinkDto.ofLink(dto);
    }

    @GetMapping
    public Stream<AccessLinkDto> findAll() {
        return this.accessLinkService.findAll()
                .map(AccessLinkDto::new)
                .map(AccessLinkDto::ofSummary);
    }

    @DeleteMapping(ID_ID)
    public void delete(@PathVariable String id) {
        this.accessLinkService.deleteById(AccessLinkDto.cleanId(id));
    }

}
