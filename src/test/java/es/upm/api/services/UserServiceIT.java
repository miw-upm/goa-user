package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.api.services.emailoutport.EmailPort;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.ForbiddenException;
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

    @Autowired
    private UserService userService;
    @Autowired
    private AccessLinkRepository accessLinkRepository;
    @MockitoBean
    private EmailPort emailPort;

    @Test
    @WithMockUser(username = "666666003", roles = {"manager"})
    void testCreate() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000001").firstName("k").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", roles = {"manager"})
    void testCreateForbidden() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("666000666").firstName("k").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", roles = {"manager"})
    void testCreateUserForbiddenByEmail() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000002").firstName("k").email("adm@gmail.com").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", roles = {"manager"})
    void testCreateForbiddenByDni() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000003").firstName("k").identity("66666601C").role(Role.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.create(userDto));
    }

    @Test
    @WithMockUser(username = "61", roles = {"manager"})
    void testReadByIdOwnerUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile("61");
        criteria.setProjection(true);
        List<User> users = this.userService.find(criteria).toList();
        assertThat(users)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(user -> user.getMobile().equals("61"));
    }

    @Test
    @WithMockUser(username = "666666003", roles = {"customer"})
    void testReadByIdOtherUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile("666666004");
        criteria.setProjection(true);
        List<User> users = this.userService.find(criteria).toList();
        assertThat(users).isEmpty();
    }

    @Test
    @WithMockUser(username = "61", roles = {"manager"})
    void testUpdateUser() {
        User oldUser = userService.readByMobile("666666002");
        oldUser.setMobile("666666666");
        this.userService.update("666666002", oldUser);
        User user = userService.readByMobile("666666666");
        assertThat(user)
                .isNotNull()
                .extracting(User::getFirstName)
                .isEqualTo(oldUser.getFirstName());
        oldUser.setMobile("666666002");
        this.userService.update("666666666", oldUser);
    }

    @Test
    @WithMockUser(username = "666666001", roles = {"customer"})
    void testUpdateByMobileWithTokenLastUsedForUpdateAt() {
        String mobile = "666666001";
        User user = this.userService.readByMobile(mobile);
        String originalCity = user.getCity();

        String token = UUID.randomUUID().toString();
        AccessLink accessLink = AccessLink.builder()
                .id(token)
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .remainingUses(2)
                .scope(UserService.SCOPE_EDIT_PROFILE)
                .build();
        this.accessLinkRepository.save(accessLink);

        user.setCity("new");
        this.userService.updateWithToken(
                mobile,
                token,
                user,
                true, false,
                DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                        .operatingSystem("Windows").deviceType("Desktop").build()
        );

        User updatedUser = this.userService.readByMobile(mobile);
        AccessLink updatedAccessLink = this.accessLinkRepository.findById(token).orElseThrow();

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCity()).isEqualTo("new");
        assertThat(updatedAccessLink.getLastUsedAt()).isNotNull();
        assertThat(updatedAccessLink.getLastUsedAt())
                .isNotNull()
                .isAfter(LocalDateTime.now().minusSeconds(5));

        updatedUser.setCity(originalCity);
        this.userService.update(mobile, updatedUser);
        this.accessLinkRepository.deleteById(token);
    }

    @Test
    @WithMockUser(username = "666666001", roles = {"customer"})
    void testUpdateByMobileWithTokenForbiddenByScope() {
        User user = this.userService.readByMobile("66");
        assertThrows(ForbiddenException.class, () ->
                this.userService.updateWithToken(
                        "66",
                        "XWBLFua2T6GLVh5wqKHB8w",
                        user,
                        true,
                        false,
                        DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                                .operatingSystem("Windows").deviceType("Desktop").build()
                )
        );
    }

    @Test
    @WithMockUser(username = "666666001", roles = {"customer"})
    void testUpdateWithTokenForbidden() {
        User user = this.userService.readByMobile("666666001");
        assertThrows(ForbiddenException.class, () ->
                this.userService.updateWithToken(
                        "666666001",
                        "GiTBDnRkS-aNYOayM69_kA",
                        user,
                        true,
                        false,
                        DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                                .operatingSystem("Windows").deviceType("Desktop").build()
                )
        );
    }

}
