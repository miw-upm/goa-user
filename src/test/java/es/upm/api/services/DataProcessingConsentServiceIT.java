package es.upm.api.services;

import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.api.services.criteria.DataProcessingConsentFindCriteria;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class DataProcessingConsentServiceIT {

    @Autowired
    private DataProcessingConsentService dataProcessingConsentService;

    @Test
    void testReadNotFound() {
        assertThrows(NotFoundException.class, () -> this.dataProcessingConsentService.read(UUID.randomUUID()));
    }

    @Test
    void testRead() {
        DataProcessingConsent consent = this.dataProcessingConsentService.read(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0100")
        );
        assertThat(consent)
                .isNotNull()
                .extracting(
                        DataProcessingConsent::getMobile,
                        DataProcessingConsent::getSignatureToken,
                        DataProcessingConsent::getDataProcessingAccepted,
                        DataProcessingConsent::getPromotionsAccepted
                )
                .containsExactly("666666000", "consent-token-0001", true, true);
    }

    @Test
    void testFindByMobile() {
        DataProcessingConsentFindCriteria criteria = new DataProcessingConsentFindCriteria();
        criteria.setMobile("666666001");
        List<DataProcessingConsent> consents = this.dataProcessingConsentService.find(criteria).toList();
        assertThat(consents)
                .isNotEmpty()
                .allMatch(consent -> "666666001".equals(consent.getMobile()));
    }

    @Test
    void testFindWithBlankMobile() {
        DataProcessingConsentFindCriteria criteria = new DataProcessingConsentFindCriteria();
        criteria.setMobile("  ");
        List<DataProcessingConsent> consents = this.dataProcessingConsentService.find(criteria).toList();
        assertThat(consents).hasSizeGreaterThanOrEqualTo(4);
    }
}
