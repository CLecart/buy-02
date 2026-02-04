package com.example.mediaservice;

import com.example.mediaservice.repository.MediaRepository;
import com.example.mediaservice.service.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
public class MediaControllerListIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    static String TEST_JWT_SECRET;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        TEST_JWT_SECRET = java.util.HexFormat.of().formatHex(secretBytes);
        r.add("APP_JWT_SECRET", () -> TEST_JWT_SECRET);
    }

    @LocalServerPort
    int port;

    @Autowired
    StorageService storageService;

    @Autowired(required = false)
    MediaRepository mediaRepository;

    @Autowired
    TestRestTemplate rest;


    @AfterEach
    void after() {
        if (mediaRepository != null) mediaRepository.deleteAll();
    }

    @Test
    void list_by_product_shouldReturnPersistedMetadata() throws Exception {
        
        String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
        byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "test.png", "image/png", pngBytes);

        String ownerId = "owner-x";
        String productId = "prod-xyz";
        storageService.store(file, ownerId, productId);

    
    org.junit.jupiter.api.Assumptions.assumeTrue(mediaRepository != null, "MediaRepository not available; skipping controller list test");

    
    com.example.shared.security.JwtService localJwt = new com.example.shared.security.JwtService(TEST_JWT_SECRET, 3_600_000L);
    String token = localJwt.generateToken(ownerId, java.util.Map.of("roles", "SELLER"));
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setBearerAuth(java.util.Objects.requireNonNull(token));
    org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

    ResponseEntity<com.example.mediaservice.dto.PagedResponse<com.example.mediaservice.dto.MediaMetadataDto>> resp = rest.exchange(
            "http://localhost:" + port + "/api/media?productId=" + productId,
            org.springframework.http.HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<com.example.mediaservice.dto.PagedResponse<com.example.mediaservice.dto.MediaMetadataDto>>() {}
    );
    assertThat(resp.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.OK);
    com.example.mediaservice.dto.PagedResponse<com.example.mediaservice.dto.MediaMetadataDto> body = resp.getBody();
    java.util.Objects.requireNonNull(body, "response body should not be null");
    List<com.example.mediaservice.dto.MediaMetadataDto> list = java.util.Objects.requireNonNull(body.getContent(), "paged content should not be null");
    assertThat(list).isNotEmpty();
    assertThat(list).anyMatch(m -> productId.equals(m.getProductId()) && ownerId.equals(m.getOwnerId()));
    }
}
