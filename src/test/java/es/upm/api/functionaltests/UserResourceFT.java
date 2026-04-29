package es.upm.api.functionaltests;

import es.upm.api.configurations.OAuth2Properties;
import es.upm.api.resources.UserResource;
import es.upm.api.resources.dtos.ProvincesDto;
import es.upm.api.resources.dtos.UserDto;
import es.upm.miw.security.Validations;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static es.upm.api.data.entities.Role.ADMIN;
import static es.upm.api.data.entities.Role.CUSTOMER;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFT {
    private static final String MOBILE_PATH = "/{id}";
    private static final UUID SEEDED_USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004");
    private static final String SEEDED_USER_MOBILE = "666666000";
    private static final String SEEDED_USER_TOKEN = "GiTBDnRkS-aNYOayM69_kA";

    private final HttpRequestBuilder httpRequestBuilder;

    @Autowired
    UserResourceFT(OAuth2Properties oAuth2Properties, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(
                testRestTemplate,
                oAuth2Properties.getApiClientId(),
                oAuth2Properties.getApiClientSecret()
        );
    }

    @Test
    void testCreate() {
        String mobile = randomMobile();
        UserDto userDto = UserDto.builder()
                .mobile(mobile)
                .firstName("ft-user")
                .role(CUSTOMER)
                .build();

        ResponseEntity<Void> createResponse = this.httpRequestBuilder
                .post(UserResource.USERS)
                .body(userDto)
                .role(ADMIN)
                .exchange(Void.class);

        ResponseEntity<UserDto> readResponse = this.httpRequestBuilder
                .get(UserResource.USERS + MOBILE_PATH, mobile)
                .role(ADMIN)
                .exchange(UserDto.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readResponse.getBody()).isNotNull();
        assertThat(readResponse.getBody().getMobile()).isEqualTo(mobile);
    }

    @Test
    void testCreateBadRequest() {
        UserDto userDto = UserDto.builder()
                .mobile("bad-mobile")
                .firstName("ft-user")
                .build();

        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(UserResource.USERS)
                .body(userDto)
                .role(ADMIN)
                .exchange(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateConflictMobile() {
        UserDto userDto = UserDto.builder()
                .mobile(SEEDED_USER_MOBILE)
                .firstName("ft-user")
                .role(CUSTOMER)
                .build();

        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(UserResource.USERS)
                .body(userDto)
                .role(ADMIN)
                .exchange(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testReadById() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(UserResource.USERS + Validations.ID_WITH_UUID, SEEDED_USER_ID)
                .role(ADMIN)
                .exchange(UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo(SEEDED_USER_MOBILE);
    }

    @Test
    void testReadByMobile() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .role(ADMIN)
                .exchange(UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo(SEEDED_USER_MOBILE);
    }

    @Test
    void testReadByMobileUnauthorized() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .role(CUSTOMER)
                .exchange(UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testReadByUrlIdWithToken() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(UserResource.USERS + UserResource.SCOPE_ID_ID_ID_TOKEN_ID, SEEDED_USER_MOBILE, SEEDED_USER_TOKEN)
                .exchange(UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo(SEEDED_USER_MOBILE);
    }

    @Test
    void testUpdate() {
        ResponseEntity<UserDto> readResponse = this.httpRequestBuilder
                .get(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .role(ADMIN)
                .exchange(UserDto.class);
        UserDto userDto = readResponse.getBody();

        assertThat(userDto).isNotNull();
        String originalFirstName = userDto.getFirstName();
        userDto.setFirstName("ft-updated");

        ResponseEntity<UserDto> updateResponse = this.httpRequestBuilder
                .put(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .body(userDto)
                .role(ADMIN)
                .exchange(UserDto.class);

        userDto.setFirstName(originalFirstName);
        ResponseEntity<UserDto> restoreResponse = this.httpRequestBuilder
                .put(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .body(userDto)
                .role(ADMIN)
                .exchange(UserDto.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getFirstName()).isEqualTo("ft-updated");
        assertThat(restoreResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testUpdateUnauthorized() {
        UserDto userDto = UserDto.builder()
                .mobile(SEEDED_USER_MOBILE)
                .firstName("ft-updated")
                .build();

        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .put(UserResource.USERS + MOBILE_PATH, SEEDED_USER_MOBILE)
                .body(userDto)
                .role(CUSTOMER)
                .exchange(UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testFind() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(UserResource.USERS)
                .role(ADMIN)
                .exchange(UserDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isNotEmpty()
                .anySatisfy(userDto -> assertThat(userDto.getMobile()).isEqualTo(SEEDED_USER_MOBILE));
    }

    @Test
    void testFindProvinces() {
        ResponseEntity<ProvincesDto> response = this.httpRequestBuilder
                .get(UserResource.USERS + UserResource.PROVINCES)
                .exchange(ProvincesDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProvinces())
                .isNotNull()
                .isNotEmpty()
                .contains("MADRID");
    }

    private String randomMobile() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 999999999));
    }
}
