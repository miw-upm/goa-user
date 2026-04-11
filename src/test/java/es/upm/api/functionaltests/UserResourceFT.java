package es.upm.api.functionaltests;

import es.upm.api.configurations.OAuth2Properties;
import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.resources.dtos.AccessLinkDto;
import es.upm.api.resources.dtos.UserDto;
import es.upm.api.services.Email;
import es.upm.api.services.SupportWebClient;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Objects;

import static es.upm.api.data.entities.Role.*;
import static es.upm.api.resources.AccessLinksResource.ACCESS_LINK;
import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFT {
    private final HttpRequestBuilder httpRequestBuilder;

    @MockitoBean
    private SupportWebClient supportWebClient;

    @Autowired
    UserResourceFT(OAuth2Properties oAuth2Properties, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(
                testRestTemplate,
                oAuth2Properties.getApiClientId(),
                oAuth2Properties.getApiClientSecret()
        );
    }

    @Test
    void testReadUser() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("61");
        assertThat(response.getBody().getFirstName()).isEqualTo("manager");
    }

    @Test
    void testReadByMobile() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "666666000").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("666666000");
        assertThat(response.getBody().getFirstName()).isEqualTo("c1");
    }

    @Test
    void testReadByMobileWithToken() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").scope("edit-profile").build();
        AccessLinkDto accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINK).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody());
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + "/" + accessLink.getMobile() + "/" + accessLink.getId()).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("666666000");
        assertThat(response.getBody().getFirstName()).isEqualTo("c1");
    }

    @Test
    void testReadByMobileWithTokenNotFoundToken() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID + "/KkK", "666666000").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadByMobileWithTokenForbiddenMobile() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE_ID_TOKEN_ID, "666666000", "GiTBDnRkS-aNYOayM69_kA").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testReadByMobileWithTokenForbiddenExpired() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE_ID_TOKEN_ID, "666666000", "hNSvhWOmQH6-NNo3gXnyow").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testReadByMobileWithTokenForbiddenUse() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        AccessLinkDto accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINK).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody());
        String link = "/" + accessLink.getMobile() + "/" + accessLink.getId();
        this.httpRequestBuilder.get(USERS + link).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + link).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + link).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + link).exchange(UserDto.class);
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + link).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testReadUserNotFound() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadUserUnauthorized() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "a4093025-cd94-40e0-986a-a15e3ad62ea8").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateWithAdmin() {
        UserDto userDto = UserDto.builder().mobile("666001666").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateConflict() {
        UserDto userDto = UserDto.builder().mobile("666666000").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testCreateBadNumber() {
        UserDto userDto = UserDto.builder().mobile("123").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateUserWithoutNumber() {
        UserDto userDto = UserDto.builder().mobile(null).firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testReadOperator() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(OPERATOR).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("c1", "c2")
                .doesNotContain("manager", "admin");
    }

    @Test
    void testFind() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("manager")
                .doesNotContain("admin");
    }

    @Test
    void testFindWithProjection() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).param("projection", "true").exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getActive).findFirst()).isNotNull();
    }

    @Test
    void testFindDoesNotContainNull() {
        ResponseEntity<String> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).exchange(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("null");
        log.debug("json: {}", response.getBody());
    }

    @Test
    void testFindAll() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(ADMIN).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testUpdate() {
        UserDto userDto = this.httpRequestBuilder.get(USERS + ID_ID, "666666000")
                .role(ADMIN).exchange(UserDto.class).getBody();

        assert userDto != null;
        String oldName = userDto.getFirstName();
        userDto.setFirstName("new");
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .put(USERS + ID_ID, "666666000").role(ADMIN).body(userDto).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getFirstName()).isEqualTo("new");
        userDto.setFirstName(oldName);
        this.httpRequestBuilder.put(USERS + ID_ID, "666666000").role(ADMIN).body(userDto).exchange(UserDto.class);

        verifyNoInteractions(this.supportWebClient);
    }

    @Test
    void testUpdateWithToken() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder().mobile("666666000").scope("edit-profile").build();
        AccessLinkDto accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINK).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody());
        BDDMockito.doNothing().when(this.supportWebClient).sendHtml(any(Email.class));
        String link = "/" + accessLink.getMobile() + "/" + accessLink.getId();
        UserDto userDto = this.httpRequestBuilder.get(USERS + link).exchange(UserDto.class).getBody();
        assert userDto != null;
        String oldName = userDto.getFirstName();
        userDto.setFirstName("new");
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .put(USERS + link).body(userDto).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getFirstName()).isEqualTo("new");
        userDto.setFirstName(oldName);
        this.httpRequestBuilder.put(USERS + link).body(userDto).exchange(UserDto.class);
        verify(this.supportWebClient, atLeastOnce()).sendHtml(any(Email.class));
    }

}
