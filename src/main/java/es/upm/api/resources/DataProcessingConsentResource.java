package es.upm.api.resources;

import es.upm.api.resources.dtos.DataProcessingConsentDto;
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

@Log4j2
@RequiredArgsConstructor
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(DataProcessingConsentResource.CONSENTS)
public class DataProcessingConsentResource {
    public static final String CONSENTS = "/consents";

    private final DataProcessingConsentService dataProcessingConsentService;

    @GetMapping(Validations.ID_WITH_UUID)
    public DataProcessingConsentDto read(@PathVariable UUID id) {
        return new DataProcessingConsentDto(this.dataProcessingConsentService.read(id));
    }

    @GetMapping
    public List<DataProcessingConsentDto> find(@ModelAttribute DataProcessingConsentFindCriteria criteria) {
        return this.dataProcessingConsentService.find(criteria)
                .map(DataProcessingConsentDto::new)
                .map(DataProcessingConsentDto::ofMobileFullNameSignatureAt)
                .toList();
    }

}
