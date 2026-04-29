package es.upm.api.services;

import es.upm.api.services.infrastructure.LegalPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LegalPolicyServiceTest {

    @Autowired
    private LegalPolicyService legalPolicyService;

    @Test
    void testCurrentPolicyVersion() {
        String version = this.legalPolicyService.currentPolicyVersion();

        assertThat(version)
                .isNotNull()
                .matches("\\d{4}-\\d{2}-\\d{2}");
    }

}
