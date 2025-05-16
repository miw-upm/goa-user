package es.upm.api.functionaltests;

import es.upm.api.data.entities.CreationAccessLink;
import es.upm.api.resources.view.AccessLinkDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static es.upm.api.data.entities.Role.ADMIN;
import static es.upm.api.data.entities.Role.CUSTOMER;
import static es.upm.api.resources.AccessLinksResource.ACCESS_LINKS;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccessLinkResourceFT {
    private final HttpRequestBuilder httpRequestBuilder;

    @Autowired
    AccessLinkResourceFT(@Value("${spring.security.oauth2.clients.api-client-id}") String apiClientId, @Value("${spring.security.oauth2.clients.api-client-secret}") String apiClientSecret, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(testRestTemplate, apiClientId, apiClientSecret);
    }

    @Test
    void testCreate() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").purpose("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessLink()).contains("666666000/");
    }

    @Test
    void testCreateNotPurpose() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateNotMobile() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .purpose("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateBadMobile() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("123000123").purpose("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(ADMIN).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateUnauthorized() {
        CreationAccessLink creationAccessLink = CreationAccessLink.builder()
                .mobile("666666000").purpose("EDIT_PROFILE").build();
        ResponseEntity<AccessLinkDto> response = this.httpRequestBuilder.post(ACCESS_LINKS).body(creationAccessLink)
                .role(CUSTOMER).exchange(AccessLinkDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
