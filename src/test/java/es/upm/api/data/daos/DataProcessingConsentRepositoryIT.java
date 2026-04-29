package es.upm.api.data.daos;

import es.upm.api.data.entities.DataProcessingConsent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DataProcessingConsentRepositoryIT {

    @Autowired
    private DataProcessingConsentRepository dataProcessingConsentRepository;

    @Test
    void testFindByMobileIn() {
        List<String> mobiles = List.of("600000100", "600000101");
        List<DataProcessingConsent> consents = this.dataProcessingConsentRepository.findByMobileIn(mobiles);

        assertThat(consents)
                .isNotEmpty()
                .allMatch(consent -> mobiles.contains(consent.getMobile()));
    }

    @Test
    void testFindByMobileInEmpty() {
        assertThat(this.dataProcessingConsentRepository.findByMobileIn(List.of()))
                .isEmpty();
    }
}
