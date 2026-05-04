package es.upm.api.infrastructure.migrations;

import es.upm.api.data.daos.DataProcessingConsentRepository;
import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.api.infrastructure.support.HashService;
import es.upm.miw.device.DeviceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("dev")
public class DataProcessingConsentMigration implements ApplicationRunner {

    private final HashService hashService;
    private final DataProcessingConsentRepository dataProcessingConsentRepository;

    @Override
    public void run(ApplicationArguments args) {
        int migratedConsents = 0;
        for (DataProcessingConsent consent : dataProcessingConsentRepository.findAll()) {
            if (migrateConsent(consent)) {
                dataProcessingConsentRepository.save(consent);
                migratedConsents++;
            }
        }
        log.warn("Data processing consent migration finished. Migrated consents: {}", migratedConsents);
    }

    private boolean migrateConsent(DataProcessingConsent consent) {
        boolean changed = false;
        String signerIdentity = this.migrateHashedField(consent.getSignerIdentity());
        if (!Objects.equals(consent.getSignerIdentity(), signerIdentity)) {
            consent.setSignerIdentity(signerIdentity);
            changed = true;
        }
        String signerEmail = this.migrateHashedField(consent.getSignerEmail());
        if (!Objects.equals(consent.getSignerEmail(), signerEmail)) {
            consent.setSignerEmail(signerEmail);
            changed = true;
        }
        String signatureToken = this.migrateHashedField(consent.getSignatureToken());
        if (!Objects.equals(consent.getSignatureToken(), signatureToken)) {
            consent.setSignatureToken(signatureToken);
            changed = true;
        }
        if (consent.getDeviceInfo() != null) {
            String ipAddress = this.hashService.hash(consent.getDeviceInfo().getIpAddress());
            if (!Objects.equals(consent.getDeviceInfo().getIpAddress(), ipAddress)) {
                consent.getDeviceInfo().setIpAddress(ipAddress);
                changed = true;
            }
        }
        return changed;
    }

    private String migrateHashedField(String value) {
        return this.hashService.hash(value);
    }
}