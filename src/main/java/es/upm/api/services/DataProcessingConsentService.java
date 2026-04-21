package es.upm.api.services;

import es.upm.api.data.daos.DataProcessingConsentRepository;
import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.DataProcessingConsentCreationDto;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DataProcessingConsentService {

    private final DataProcessingConsentRepository dataProcessingConsentRepository;
    private final LegalPolicyService legalPolicyService;

    @Autowired
    public DataProcessingConsentService(DataProcessingConsentRepository dataProcessingConsentRepository,
                                        LegalPolicyService legalPolicyService) {
        this.dataProcessingConsentRepository = dataProcessingConsentRepository;
        this.legalPolicyService = legalPolicyService;
    }

    public void create(User signer, String signatureToken, DataProcessingConsentCreationDto consentCreation, DeviceInfo deviceInfo) {
        DataProcessingConsent consent = DataProcessingConsent.builder()
                .id(UUID.randomUUID())
                .signatureAt(LocalDateTime.now())
                .signer(signer)
                .signerFullName(signer.fullName())
                .signerIdentity(signer.getIdentity())
                .mobile(signer.getMobile())
                .signerEmail(signer.getEmail())
                .signatureToken(signatureToken)
                .deviceInfo(deviceInfo)
                .policyVersion(this.legalPolicyService.currentPolicyVersion())
                .dataProcessingAccepted(consentCreation.getDataProcessingAccepted())
                .promotionsAccepted(consentCreation.getPromotionsAccepted())
                .build();
        this.dataProcessingConsentRepository.save(consent);
    }

    public DataProcessingConsent read(UUID id) {
        return this.dataProcessingConsentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public Stream<DataProcessingConsent> find(DataProcessingConsentFindCriteria criteria) {
        if (Objects.isNull(criteria.getMobile()) || criteria.getMobile().isBlank()) {
            return this.dataProcessingConsentRepository.findAll().stream();
        }
        return this.dataProcessingConsentRepository.findByMobile(criteria.getMobile()).stream();
    }

}
