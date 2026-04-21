package es.upm.api.resources;

import es.upm.api.resources.dtos.DataProcessingConsentDto;
import es.upm.api.resources.dtos.validations.Validations;
import es.upm.api.services.DataProcessingConsentFindCriteria;
import es.upm.api.services.DataProcessingConsentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@PreAuthorize(Security.AUTHENTICATED)
@RestController
@RequestMapping(DataProcessingConsentResource.CONSENTS)
public class DataProcessingConsentResource {
    public static final String CONSENTS = "/consents";

    private final DataProcessingConsentService dataProcessingConsentService;

    @Autowired
    public DataProcessingConsentResource(DataProcessingConsentService dataProcessingConsentService) {
        this.dataProcessingConsentService = dataProcessingConsentService;
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @GetMapping(Validations.ID_WITH_UUID)
    public DataProcessingConsentDto readById(@PathVariable UUID id) {
        return new DataProcessingConsentDto(this.dataProcessingConsentService.read(id));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @GetMapping
    public List<DataProcessingConsentDto> findNullSafe(@ModelAttribute DataProcessingConsentFindCriteria criteria) {
        return this.dataProcessingConsentService.findNullSafe(criteria)
                .map(DataProcessingConsentDto::new)
                .map(DataProcessingConsentDto::ofMobileFullNameSignatureAt).toList();
    }

}
