package es.upm.api.resources;

import es.upm.api.resources.dtos.AccessLinkCreationDto;
import es.upm.api.resources.dtos.AccessLinkDto;
import es.upm.api.services.AccessLinkFindCriteria;
import es.upm.api.services.AccessLinkService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public AccessLinkDto create(@Valid @RequestBody AccessLinkCreationDto accessLinkCreationDto) {
        return new AccessLinkDto(accessLinkService.create(accessLinkCreationDto));
    }

    @GetMapping
    public List<AccessLinkDto> findNullSafe(@ModelAttribute AccessLinkFindCriteria criteria) {
        return this.accessLinkService.findNullSafe(criteria)
                .map(AccessLinkDto::new)
                .toList();
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable String id) {
        this.accessLinkService.deleteById(id);
    }

    @GetMapping(ID_ID)
    public AccessLinkDto read(@PathVariable String id) {
        return new AccessLinkDto(this.accessLinkService.read(id));
    }

}
