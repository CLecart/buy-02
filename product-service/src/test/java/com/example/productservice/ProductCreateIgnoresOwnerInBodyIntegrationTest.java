package com.example.productservice;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class ProductCreateIgnoresOwnerInBodyIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    static String testJwtSecret;

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        testJwtSecret = java.util.HexFormat.of().formatHex(secretBytes);
        reg.add("APP_JWT_SECRET", () -> testJwtSecret);
    }

    @Autowired
    com.example.shared.security.JwtService jwtService;

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ProductRepository productRepository;

    @AfterEach
    void after() { productRepository.deleteAll(); }

    @Test
    void create_shouldUseJwtSubject_notClientProvidedOwnerId() {
    String realOwner = "real-owner";
    String token = Objects.requireNonNull(jwtService.generateToken(realOwner, java.util.Map.of("roles", "SELLER")), "jwt token must not be null in test");

    ProductDto req = new ProductDto(null, "Tricky", "desc", BigDecimal.valueOf(7.77), "malicious-owner");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(token);

    ResponseEntity<ProductDto> resp = rest.postForEntity("http://localhost:" + port + "/api/products", new HttpEntity<>(req, headers), ProductDto.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ProductDto body = Objects.requireNonNull(resp.getBody(), "response body must not be null for created product");
    assertThat(body.getOwnerId()).isEqualTo(realOwner);
    }
}
