package es.upm.api.services;

import es.upm.api.data.daos.DataProcessingConsentRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.api.data.entities.User;
import es.upm.api.services.criteria.DataProcessingConsentFindCriteria;
import es.upm.api.services.utils.LegalPolicyService;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static es.upm.api.data.entities.Role.CUSTOMER;

@RequiredArgsConstructor
@Service
public class DataProcessingConsentService {

    private final DataProcessingConsentRepository dataProcessingConsentRepository;
    private final UserRepository userRepository;
    private final LegalPolicyService legalPolicyService;

    public void create(User signer, String signatureToken, boolean dataProcessingAccepted, boolean promotionsAccepted,
                       DeviceInfo deviceInfo) {
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
                .dataProcessingAccepted(dataProcessingAccepted)
                .promotionsAccepted(promotionsAccepted)
                .build();
        this.dataProcessingConsentRepository.save(consent);
    }

    public DataProcessingConsent read(UUID id) {
        return this.dataProcessingConsentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public Stream<DataProcessingConsent> find(DataProcessingConsentFindCriteria criteria) {
        if (Objects.isNull(criteria.getAttribute()) || criteria.getAttribute().isBlank()) {
            return this.dataProcessingConsentRepository.findAll().stream();
        }
        List<String> mobiles = this.userRepository.findByAll(criteria.getAttribute(), List.of(CUSTOMER)).stream()
                .map(User::getMobile).toList();
        return this.dataProcessingConsentRepository.findByMobileIn(mobiles).stream();
    }

}
