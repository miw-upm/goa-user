package es.upm.api.functionaltests;

import es.upm.api.configurations.OAuth2Properties;
import es.upm.api.resources.dtos.AccessLinkCreationDto;
import es.upm.api.resources.dtos.AccessLinkDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static es.upm.api.data.entities.Role.*;
import static es.upm.api.resources.AccessLinksResource.ACCESS_LINK;
import static es.upm.api.resources.AccessLinksResource.ID_ID;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccessLinkResourceFT {
    private final HttpRequestBuilder httpRequestBuilder;

    @Autowired
    AccessLinkResourceFT(OAuth2Properties oAuth2Properties, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(
                testRestTemplate,
                oAuth2Properties.getApiClientId(),
                oAuth2Properties.getApiClientSecret()
        );
    }

    @Test
    void testCreate() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("666666000").scope("edit-profile").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).contains("666666000");
    }

    @Test
    void testCreateBadRequestScope() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("666666000").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateBadRequestMobile() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .scope("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateNotFoundMobile() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("123000123").scope("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateUnauthorized() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(CUSTOMER).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testFindAll() {
        ResponseEntity<AccessLinkDto[]> response = this.httpRequestBuilder.get(ACCESS_LINK)
                .role(ADMIN).exchange(AccessLinkDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotEmpty()
                .allSatisfy(accessLink -> {
                    assertThat(accessLink.getMobile()).isNotNull();
                });
    }

    @Test
    void testDelete() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(ADMIN).exchange(AccessLinkDto.class);
        AccessLinkDto link = Objects.requireNonNull(response.getBody());
        ResponseEntity<Void> response2 = this.httpRequestBuilder
                .delete(ACCESS_LINK + ID_ID, link.getId()).role(ADMIN).exchange(Void.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDeleteForbidden() {
        AccessLinkCreationDto accessLinkCreationDto = AccessLinkCreationDto.builder()
                .mobile("666666000").scope("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINK).body(accessLinkCreationDto)
                .role(MANAGER).exchange(AccessLinkDto.class);
        AccessLinkDto link = Objects.requireNonNull(response.getBody());
        ResponseEntity<Void> response2 = this.httpRequestBuilder
                .delete(ACCESS_LINK + ID_ID, link.getId()).role(MANAGER).exchange(Void.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

}
