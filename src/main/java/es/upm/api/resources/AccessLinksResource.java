package es.upm.api.resources;

import es.upm.api.resources.dtos.AccessLinkCreationDto;
import es.upm.api.resources.dtos.AccessLinkDto;
import es.upm.api.resources.dtos.validations.Validations;
import es.upm.api.services.criteria.AccessLinkFindCriteria;
import es.upm.api.services.AccessLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AccessLinksResource.ACCESS_LINK)
public class AccessLinksResource {
    public static final String ACCESS_LINK = "/access-link";

    private final AccessLinkService accessLinkService;

    @PostMapping
    public AccessLinkDto create(@Valid @RequestBody AccessLinkCreationDto accessLinkCreationDto) {
        return new AccessLinkDto(
                accessLinkService.create(accessLinkCreationDto.getMobile(),accessLinkCreationDto.getScope())
        );
    }

    @GetMapping(Validations.ID_WITH_UUID)
    public AccessLinkDto read(@PathVariable String id) {
        return new AccessLinkDto(this.accessLinkService.read(id));
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(Validations.ID_WITH_UUID_BASE64)
    public void delete(@PathVariable String id) {
        this.accessLinkService.deleteById(id);
    }

    @GetMapping
    public List<AccessLinkDto> find(@ModelAttribute AccessLinkFindCriteria criteria) {
        return this.accessLinkService.find(criteria)
                .map(AccessLinkDto::new)
                .toList();
    }
}
