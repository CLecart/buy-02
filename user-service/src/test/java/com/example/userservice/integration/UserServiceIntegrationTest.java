package com.example.userservice.integration;

import com.example.userservice.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import com.example.shared.security.JwtService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.model.User;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class UserServiceIntegrationTest {

    

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    static String TEST_JWT_SECRET;

    @DynamicPropertySource
    static void registerMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        TEST_JWT_SECRET = java.util.HexFormat.of().formatHex(secretBytes);
        registry.add("APP_JWT_SECRET", () -> TEST_JWT_SECRET);
    }

    @Test
    void signup_then_signin_flow() throws Exception {
        String base = "http://localhost:" + port;

        SignupRequest req = new SignupRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("password123");
        req.setRole(com.example.userservice.model.Role.SELLER);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON));

    
    HttpEntity<SignupRequest> entity = new HttpEntity<>(req, headers);
    ResponseEntity<String> respStr = rest.postForEntity(base + "/api/auth/signup", entity, String.class);
        assertThat(respStr.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String body = Objects.requireNonNull(respStr.getBody(), "signup response body must not be null");

    
    ObjectMapper om = new ObjectMapper();
    JsonNode root = om.readTree(body);
    String signupToken = Objects.requireNonNull(root.path("token").asText(null), "signup token must not be null");
    assertThat(body).as("signup raw body").contains("token");
    assertThat(signupToken).as("signup response body: %s", body).isNotBlank();

    
    User saved = userRepository.findByEmail("alice@example.com").orElse(null);
    assertThat(saved).as("user persisted").isNotNull();
    assertThat(saved.getId()).as("saved user id").isNotBlank();

    var signinClaims = jwtService.parseToken(signupToken);
        assertThat(signinClaims.getSubject()).isNotBlank();
        assertThat(signinClaims.get("roles", String.class)).isEqualTo("SELLER");
    }
}
