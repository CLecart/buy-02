package com.example.productservice;

import com.example.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying role-based access control.
 * Only SELLER role should be able to create/update/delete products.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleBasedAccessControlTest {

    static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(
            DockerImageName.parse("mongo:6.0")
    );

    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    static {
        Startables.deepStart(MONGO_CONTAINER, KAFKA_CONTAINER).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.listener.auto-startup", () -> "true");
        registry.add("APP_JWT_SECRET", () -> "test-secret-key-for-jwt-signing-minimum-32-characters");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Test
    void clientRole_cannotCreateProduct() {
        // Generate token with CLIENT role
        String clientToken = Objects.requireNonNull(
            jwtService.generateToken("client-user-123", Map.of("roles", "CLIENT")),
            "client token"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(clientToken);

        String productJson = """
            {
                "name": "Test Product",
                "price": 10.00
            }
            """;

        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/products",
            request,
            String.class
        );

        // CLIENT should be forbidden from creating products
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
            "CLIENT role should not be able to create products");
    }

    @Test
    void sellerRole_canCreateProduct() {
        // Generate token with SELLER role
        String sellerToken = Objects.requireNonNull(
            jwtService.generateToken("seller-user-456", Map.of("roles", "SELLER")),
            "seller token"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sellerToken);

        String productJson = """
            {
                "name": "Seller Product",
                "price": 25.50
            }
            """;

        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/products",
            request,
            String.class
        );

        // SELLER should be able to create products
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "SELLER role should be able to create products");
    }

    @Test
    void unauthenticated_cannotCreateProduct() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No Authorization header

        String productJson = """
            {
                "name": "Anonymous Product",
                "price": 5.00
            }
            """;

        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/products",
            request,
            String.class
        );

        // Unauthenticated should be rejected (401 or 403)
        assertTrue(
            response.getStatusCode() == HttpStatus.UNAUTHORIZED || 
            response.getStatusCode() == HttpStatus.FORBIDDEN,
            "Unauthenticated requests should not be able to create products"
        );
    }

    @Test
    void anyUser_canReadProducts() {
        // No authentication needed for GET
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/products",
            String.class
        );

        // Anyone can read products
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Product listing should be publicly accessible");
    }
}
