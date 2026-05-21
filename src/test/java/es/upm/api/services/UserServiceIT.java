package es.upm.api.services;

import es.upm.api.configurations.SeederForDev;
import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.api.infrastructure.clients.email.GoaSupportClient;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIT {
    private static final String MANAGER_MOBILE = "600000111";
    private static final String CUSTOMER_0_MOBILE = "600000100";
    private static final String CUSTOMER_1_MOBILE = "600000101";
    private static final String CUSTOMER_2_MOBILE = "600000102";

    @Autowired
    private UserService userService;
    @Autowired
    private AccessLinkRepository accessLinkRepository;
    @MockitoBean
    private GoaSupportClient goaSupportClient;

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testCreate() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000001").firstName("k").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testCreateForbidden() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("666000666").firstName("k").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testCreateUserForbiddenByEmail() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000002").firstName("k").email("adm@gmail.com").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testCreateForbiddenByDni() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000003").firstName("k").identity("66666601C").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testReadByIdOwnerUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile(MANAGER_MOBILE);
        List<User> users = this.userService.find(criteria).toList();
        assertThat(users)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(user -> user.getMobile().equals(MANAGER_MOBILE));
    }

    @Test
    @WithMockUser(username = CUSTOMER_0_MOBILE, roles = {"customer"})
    void testReadByIdOtherUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile(CUSTOMER_1_MOBILE);
        List<User> users = this.userService.find(criteria).toList();
        assertThat(users).isEmpty();
    }

    @Test
    @WithMockUser(username = MANAGER_MOBILE, roles = {"manager"})
    void testUpdateUser() {
        User oldUser = userService.readByMobile(CUSTOMER_2_MOBILE);
        UUID id = oldUser.getId();
        oldUser.setMobile("600099999");
        oldUser.setPassword(null);
        this.userService.update(id, oldUser);
        User user = userService.readByMobile("600099999");
        assertThat(user)
                .isNotNull()
                .extracting(User::getFirstName)
                .isEqualTo(oldUser.getFirstName());
        oldUser.setMobile(CUSTOMER_2_MOBILE);
        this.userService.update(id, oldUser);
    }

    @Test
    @WithMockUser(username = CUSTOMER_0_MOBILE, roles = {"customer"})
    void testUpdateByMobileWithTokenLastUsedForUpdateAt() {
        String mobile = CUSTOMER_0_MOBILE;
        User user = this.userService.readByMobile(mobile);
        String originalCity = user.getCity();

        user.setCity("new");
        user.setPassword(null);
        this.userService.updateByUrlIdWithToken(
                UserService.SCOPE_EDIT_PROFILE,
                SeederForDev.URL_0,
                SeederForDev.TOKEN_0,
                user,
                true, false,
                DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                        .operatingSystem("Windows").deviceType("Desktop").build()
        );

        User updatedUser = this.userService.readByMobile(mobile);
        var updatedAccessLink = this.accessLinkRepository.findByUrlId(SeederForDev.URL_0).orElseThrow();

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCity()).isEqualTo("new");
        assertThat(updatedAccessLink.getLastUsedAt()).isNotNull();
        assertThat(updatedAccessLink.getLastUsedAt())
                .isNotNull()
                .isAfter(LocalDateTime.now().minusSeconds(5));

        updatedUser.setCity(originalCity);
        updatedUser.setPassword(null);
        this.userService.update(updatedUser.getId(), updatedUser);
    }

    @Test
    @WithMockUser(username = CUSTOMER_2_MOBILE, roles = {"customer"})
    void testUpdateByMobileWithTokenForbiddenByScope() {
        User user = this.userService.readByMobile(CUSTOMER_2_MOBILE);
        assertThrows(ForbiddenException.class, () ->
                this.userService.updateByUrlIdWithToken(
                        UserService.SCOPE_EDIT_PROFILE,
                        SeederForDev.URL_2,
                        SeederForDev.TOKEN_2,
                        user,
                        true,
                        false,
                        DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                                .operatingSystem("Windows").deviceType("Desktop").build()
                )
        );
    }

    @Test
    @WithMockUser(username = CUSTOMER_0_MOBILE, roles = {"customer"})
    void testUpdateByUrlIdWithTokenForbidden() {
        User user = this.userService.readByMobile(CUSTOMER_0_MOBILE);
        assertThrows(UnauthorizedException.class, () ->
                this.userService.updateByUrlIdWithToken(
                        UserService.SCOPE_EDIT_PROFILE,
                        SeederForDev.URL_0,
                        SeederForDev.TOKEN_1,
                        user,
                        true,
                        false,
                        DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                                .operatingSystem("Windows").deviceType("Desktop").build()
                )
        );
    }

}
