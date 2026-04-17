package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AccessLinkRepositoryIT {

    @Autowired
    private AccessLinkRepository accessLinkRepository;

    @Test
    void testFindActiveUsedAndByMobileAndByScopeWithoutFilters() {
        List<AccessLink> result = this.accessLinkRepository
                .searchActiveUsedByUserIdsAndScope(null, null);

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getExpiresAt().isAfter(LocalDateTime.now()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByMobileAndByScopeWithUserId() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005");

        List<AccessLink> result = this.accessLinkRepository
                .searchActiveUsedByUserIdsAndScope(List.of(userId), null);

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getUser().getId().equals(userId))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByMobileAndByScopeWithScope() {
        List<AccessLink> result = this.accessLinkRepository
                .searchActiveUsedByUserIdsAndScope(null, "edit-profile");

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> "edit-profile".equals(link.getScope()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByMobileAndByScopeWithBothFilters() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005");

        List<AccessLink> result = this.accessLinkRepository
                .searchActiveUsedByUserIdsAndScope(List.of(userId), "edit-profile");

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getUser().getId().equals(userId))
                .allMatch(link -> "edit-profile".equals(link.getScope()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByMobileAndByScopeReturnsEmptyForExpiredOrUnused() {
        List<AccessLink> result = this.accessLinkRepository
                .searchActiveUsedByUserIdsAndScope(List.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004")), null);

        // users[3] tiene links pero ninguno con lastUsedForUpdateAt != null
        assertThat(result).isEmpty();
    }
}
