package es.upm.api.resources;

import es.upm.api.resources.dtos.AccessLinkCreationDto;
import es.upm.api.resources.dtos.AccessLinkResponseDto;
import es.upm.api.services.AccessLinkCreationResult;
import es.upm.api.services.AccessLinkService;
import es.upm.api.services.criteria.AccessLinkFindCriteria;
import es.upm.miw.security.Security;
import es.upm.miw.security.Validations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AccessLinksResource.ACCESS_LINKS)
@RequiredArgsConstructor
@Log4j2
public class AccessLinksResource {
    public static final String ACCESS_LINKS = "/access-links";
    public static final String CONSUME = "/consume";
    public static final String SCOPE_ID = "/{scope}";
    private final AccessLinkService accessLinkService;

    @PostMapping
    public AccessLinkResponseDto create(@Valid @RequestBody AccessLinkCreationDto accessLinkCreationDto) {
        AccessLinkCreationResult result = accessLinkService.create(accessLinkCreationDto.getMobile(),
                accessLinkCreationDto.getScope(), accessLinkCreationDto.getDocumentId());
        return new AccessLinkResponseDto(result);
    }

    @GetMapping(Validations.ID_WITH_UUID)
    public AccessLinkResponseDto read(@PathVariable UUID id) {
        return new AccessLinkResponseDto(this.accessLinkService.read(id));
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(Validations.ID_WITH_UUID)
    public void delete(@PathVariable UUID id) {
        this.accessLinkService.delete(id);
    }

    @GetMapping
    public List<AccessLinkResponseDto> find(@ModelAttribute AccessLinkFindCriteria criteria) {
        return this.accessLinkService.find(criteria)
                .map(AccessLinkResponseDto::new)
                .map(AccessLinkResponseDto::ofSummary)
                .toList();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @PostMapping(SCOPE_ID + Validations.ID_WITH_UUID_BASE64 + CONSUME)
    public AccessLinkResponseDto consumeToken(@PathVariable String scope, @PathVariable String id, @RequestBody String token) {
        return new AccessLinkResponseDto(this.accessLinkService.consumeToken(scope, id, token));
    }
}
