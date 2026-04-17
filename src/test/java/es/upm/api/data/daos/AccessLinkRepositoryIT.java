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
    void testFindActiveUsedAndByUserIdAndByScopeWithoutFilters() {
        List<AccessLink> result = this.accessLinkRepository
                .findActiveUsedAndByUserIdAndByScope( null, null);

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getExpiresAt().isAfter(LocalDateTime.now()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByUserIdAndByScopeWithUserId() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005");

        List<AccessLink> result = this.accessLinkRepository
                .findActiveUsedAndByUserIdAndByScope( userId, null);

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getUser().getId().equals(userId))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByUserIdAndByScopeWithScope() {
        List<AccessLink> result = this.accessLinkRepository
                .findActiveUsedAndByUserIdAndByScope(null, "edit-profile");

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> "edit-profile".equals(link.getScope()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByUserIdAndByScopeWithBothFilters() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005");

        List<AccessLink> result = this.accessLinkRepository
                .findActiveUsedAndByUserIdAndByScope( userId, "edit-profile");

        assertThat(result)
                .isNotEmpty()
                .allMatch(link -> link.getUser().getId().equals(userId))
                .allMatch(link -> "edit-profile".equals(link.getScope()))
                .allMatch(link -> link.getLastUsedForUpdateAt() != null);
    }

    @Test
    void testFindActiveUsedAndByUserIdAndByScopeReturnsEmptyForExpiredOrUnused() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004");

        List<AccessLink> result = this.accessLinkRepository
                .findActiveUsedAndByUserIdAndByScope( userId, null);

        // users[3] tiene links pero ninguno con lastUsedForUpdateAt != null
        assertThat(result).isEmpty();
    }
}
