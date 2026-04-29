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
    void testFindByMobileAndFirstNameAndFamilyNameNullSafeWithMobile() {
        System.out.println(this.userRepository.findAll());
        assertThat(this.userRepository.findCustomers(
                "1", null, null, List.of(CUSTOMER)))
                .anyMatch(user -> "600000101".equals(user.getMobile()));
    }

    @Test
    void testFindByMobileAndFirstNameAndFamilyNameNullSafeWithFamilyName() {
        assertThat(this.userRepository.findCustomers(
                null, null, "ruiz", List.of(ADMIN, MANAGER, OPERATOR, CUSTOMER)))
                .isNotEmpty();
    }

    @Test
    void testFindByAll() {
        assertThat(this.userRepository.findCustomersByText("1", List.of(CUSTOMER)))
                .allMatch(user -> user.getRole().equals(CUSTOMER));
    }
}
