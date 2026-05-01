package es.upm.api.resources;

import es.upm.api.resources.dtos.DataProcessingConsentResponseDto;
import es.upm.api.services.DataProcessingConsentService;
import es.upm.api.services.criteria.DataProcessingConsentFindCriteria;
import es.upm.miw.security.Security;
import es.upm.miw.security.Validations;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(DataProcessingConsentResource.CONSENTS)
@RequiredArgsConstructor
@Log4j2
public class DataProcessingConsentResource {
    public static final String CONSENTS = "/consents";

    private final DataProcessingConsentService dataProcessingConsentService;

    @GetMapping(Validations.ID_WITH_UUID)
    public DataProcessingConsentResponseDto read(@PathVariable UUID id) {
        return new DataProcessingConsentResponseDto(this.dataProcessingConsentService.read(id));
    }

    @GetMapping
    public List<DataProcessingConsentResponseDto> find(@ModelAttribute DataProcessingConsentFindCriteria criteria) {
        return this.dataProcessingConsentService.find(criteria)
                .map(DataProcessingConsentResponseDto::new)
                .map(DataProcessingConsentResponseDto::ofSummary)
                .toList();
    }

}
