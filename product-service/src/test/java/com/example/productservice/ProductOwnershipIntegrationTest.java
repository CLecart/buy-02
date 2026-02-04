package com.example.productservice;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class ProductOwnershipIntegrationTest {

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
    void update_and_delete_forbidden_for_non_owner() {
    String ownerA = "owner-a";
    String tokenA = Objects.requireNonNull(jwtService.generateToken(ownerA, java.util.Map.of("roles", "SELLER")), "jwt token must not be null in test");
    HttpHeaders headersA = new HttpHeaders();
    headersA.setContentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON));
        headersA.setBearerAuth(tokenA);
        ProductDto req = new ProductDto(null, "Secret Product", "desc", BigDecimal.valueOf(4.5));
        ResponseEntity<ProductDto> resp = rest.postForEntity("http://localhost:"+port+"/api/products", new HttpEntity<>(req, headersA), ProductDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String id = Objects.requireNonNull(resp.getBody(), "created product response must not be null").getId();

    String ownerB = "owner-b";
    String tokenB = Objects.requireNonNull(jwtService.generateToken(ownerB, java.util.Map.of("roles", "SELLER")), "jwt token must not be null in test");
    HttpHeaders headersB = new HttpHeaders();
    headersB.setContentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON));
        headersB.setBearerAuth(tokenB);
        ProductDto update = new ProductDto(null, "Hacked", "x", BigDecimal.valueOf(1.0));
        ResponseEntity<Void> updateResp = rest.exchange("http://localhost:"+port+"/api/products/"+id, HttpMethod.PUT, new HttpEntity<>(update, headersB), Void.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    ResponseEntity<Void> delResp = rest.exchange("http://localhost:"+port+"/api/products/"+id, HttpMethod.DELETE, new HttpEntity<>(headersB), Void.class);
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    ResponseEntity<Void> delA = rest.exchange("http://localhost:"+port+"/api/products/"+id, HttpMethod.DELETE, new HttpEntity<>(headersA), Void.class);
        assertThat(delA.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
