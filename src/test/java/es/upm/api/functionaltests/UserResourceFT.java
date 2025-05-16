package es.upm.api.functionaltests;

import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.resources.view.AccessLinkDto;
import es.upm.api.resources.view.UserDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Objects;

import static es.upm.api.data.entities.Role.*;
import static es.upm.api.resources.AccessLinksResource.ACCESS_LINKS;
import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFT {
    private final HttpRequestBuilder httpRequestBuilder;

    @Autowired
    UserResourceFT(@Value("${spring.security.oauth2.clients.api-client-id}") String apiClientId, @Value("${spring.security.oauth2.clients.api-client-secret}") String apiClientSecret, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(testRestTemplate, apiClientId, apiClientSecret);
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
                .get(USERS + MOBILE + MOBILE_ID, "66").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("66");
        assertThat(response.getBody().getFirstName()).isEqualTo("customer");
    }

    @Test
    void testReadByMobileWithToken() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        String accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody()).getAccessLink();
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("666666000");
        assertThat(response.getBody().getFirstName()).isEqualTo("c1");
    }

    @Test
    void testReadByMobileWithTokenNotFoundToken() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + MOBILE_ID + "/KkK", "666666000").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadByMobileWithTokenForbiddenMobile() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + MOBILE_ID + "/GiTBDnRkS-aNYOayM69_kA", "666666000").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testReadByMobileWithTokenForbiddenExpired() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + MOBILE_ID + "/hNSvhWOmQH6-NNo3gXnyow", "666666000").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testReadByMobileWithTokenForbiddenUse() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        String accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody()).getAccessLink();
        this.httpRequestBuilder.get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        this.httpRequestBuilder.get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + accessLink).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
        UserDto userDto = UserDto.builder().mobile("1").firstName("daemon").build();
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
    void testSearch() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("manager")
                .doesNotContain("admin");
    }

    @Test
    void testSearchDoesNotContainNull() {
        ResponseEntity<String> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).exchange(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("null");
        log.debug("json: {}", response.getBody());
    }

    @Test
    void testUpdateWithToken() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder().mobile("666666000").scope("EDIT_PROFILE").build();
        String accessLink = Objects.requireNonNull(this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class).getBody()).getAccessLink();
        UserDto userDto = this.httpRequestBuilder.get(USERS + MOBILE + accessLink).exchange(UserDto.class).getBody();
        assert userDto != null;
        String oldName = userDto.getFirstName();
        userDto.setFirstName("new");
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .put(USERS + accessLink).body(userDto).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getFirstName()).isEqualTo("new");
        userDto.setFirstName(oldName);
        this.httpRequestBuilder.put(USERS + accessLink).body(userDto).exchange(UserDto.class);
    }

}
