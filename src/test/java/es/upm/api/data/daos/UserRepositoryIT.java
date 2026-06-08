package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static es.upm.api.data.entities.Role.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByMobile() {
        assertThat(this.userRepository.findByMobile("600000110")).isPresent();
    }

    @Test
    void testFindByScopeIn() {
        List<Role> roles = List.of(ADMIN, MANAGER);
        assertThat(this.userRepository.findByRoleIn(roles))
                .isNotEmpty()
                .allMatch(user -> roles.contains(user.getRole()));
    }

    @Test
    void testFindUsersByCustomerTextWithMobile() {
        assertThat(this.userRepository.findUsers(
                "1", null, List.of(CUSTOMER)))
                .anyMatch(user -> "600000101".equals(user.getMobile()));
    }

    @Test
    void testFindUsersByCustomerTextWithFamilyName() {
        assertThat(this.userRepository.findUsers(
                "ruiz", null, List.of(ADMIN, MANAGER, OPERATOR, CUSTOMER)))
                .isNotEmpty();
    }

    @Test
    void testFindUsersByActive() {
        assertThat(this.userRepository.findUsers(null, true, List.of(ADMIN, MANAGER, OPERATOR, CUSTOMER)))
                .isNotEmpty()
                .allMatch(user -> Boolean.TRUE.equals(user.getActive()));
    }

    @Test
    void testFindByAll() {
        assertThat(this.userRepository.findCustomersByText("1", List.of(CUSTOMER)))
                .allMatch(user -> user.getRole().equals(CUSTOMER));
    }
}
