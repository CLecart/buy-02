package com.example.productservice;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.kafka.ProductEventProducer;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;


import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class ProductControllerIntegrationTest {

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

    @org.springframework.beans.factory.annotation.Autowired
    com.example.shared.security.JwtService jwtService;

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ProductRepository productRepository;

    @MockBean
    ProductEventProducer productEventProducer;

    @AfterEach
    void after() {
        productRepository.deleteAll();
    }

    @Test
    void create_and_get_product() {
    ProductDto req = new ProductDto(null, "Test Product", "desc", BigDecimal.valueOf(9.99));
    
    String ownerId = "test-user-1";
    String token = jwtService.generateToken(ownerId, java.util.Map.of("roles", "SELLER"));
    token = java.util.Objects.requireNonNull(token);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(java.util.Objects.requireNonNull(token));
    HttpEntity<ProductDto> entity = new HttpEntity<>(req, headers);
        ResponseEntity<ProductDto> resp = rest.postForEntity("http://localhost:"+port+"/api/products", entity, ProductDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ProductDto body = resp.getBody();
    assertThat(body).isNotNull();
    String id = java.util.Objects.requireNonNull(body).getId();
    assertThat(id).isNotNull();

    ResponseEntity<ProductDto> get = rest.getForEntity("http://localhost:"+port+"/api/products/"+id, ProductDto.class);
    assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
    ProductDto got = java.util.Objects.requireNonNull(get.getBody());
    assertThat(got.getName()).isEqualTo("Test Product");
    }

    @Test
    void list_pagination_and_search() throws Exception {
        
        productRepository.save(new com.example.productservice.model.Product("Apple", "fruits", java.math.BigDecimal.valueOf(1.0), "owner-a"));
        productRepository.save(new com.example.productservice.model.Product("Banana", "fruits", java.math.BigDecimal.valueOf(2.0), "owner-b"));
        productRepository.save(new com.example.productservice.model.Product("Apricot", "fruits", java.math.BigDecimal.valueOf(3.0), "owner-a"));
        productRepository.save(new com.example.productservice.model.Product("Blueberry", "fruits", java.math.BigDecimal.valueOf(4.0), "owner-c"));
        productRepository.save(new com.example.productservice.model.Product("Avocado", "fruits", java.math.BigDecimal.valueOf(5.0), "owner-d"));

        
        String url = "http://localhost:"+port+"/api/products?page=0&size=2";
    ResponseEntity<String> resp = rest.getForEntity(url, String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = Objects.requireNonNull(resp.getBody(), "list pagination response body must not be null");
    com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        com.fasterxml.jackson.databind.JsonNode content = root.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(2);
        long total = root.get("totalElements").asLong();
        assertThat(total).isEqualTo(5L);

        
        String su = "http://localhost:"+port+"/api/products?search=ap&page=0&size=10";
    ResponseEntity<String> r2 = rest.getForEntity(su, String.class);
    assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
    String r2body = Objects.requireNonNull(r2.getBody(), "search response body must not be null");
    com.fasterxml.jackson.databind.JsonNode root2 = new com.fasterxml.jackson.databind.ObjectMapper().readTree(r2body);
        com.fasterxml.jackson.databind.JsonNode content2 = root2.get("content");
        assertThat(content2.isArray()).isTrue();
        
        java.util.List<String> names = new java.util.ArrayList<>();
        content2.forEach(n -> names.add(n.get("name").asText()));
        assertThat(names).containsExactlyInAnyOrder("Apple", "Apricot");
        long total2 = root2.get("totalElements").asLong();
        assertThat(total2).isEqualTo(2L);
    }

    @Test
    void list_filters_category_price_seller_and_stock() throws Exception {
        var p1 = new com.example.productservice.model.Product("Laptop A", "gaming", new BigDecimal("900"), "seller-a", 3);
        p1.setCategory("electronics");
        productRepository.save(p1);

        var p2 = new com.example.productservice.model.Product("Laptop B", "office", new BigDecimal("700"), "seller-b", 0);
        p2.setCategory("electronics");
        productRepository.save(p2);

        var p3 = new com.example.productservice.model.Product("Chair", "home", new BigDecimal("120"), "seller-a", 7);
        p3.setCategory("furniture");
        productRepository.save(p3);

        assertFilteredCount("http://localhost:" + port + "/api/products?category=electronics&page=0&size=10", 2L);
        assertFilteredCount("http://localhost:" + port + "/api/products?sellerId=seller-a&page=0&size=10", 2L);
        assertFilteredCount("http://localhost:" + port + "/api/products?inStock=true&page=0&size=10", 2L);

        ResponseEntity<String> response = rest.getForEntity(
            "http://localhost:" + port + "/api/products?category=electronics&sellerId=seller-a&inStock=true&page=0&size=10",
            String.class
        );
        String body = Objects.requireNonNull(response.getBody(), "combined-filter response body must not be null");
        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        assertThat(root.get("totalElements").asLong()).isEqualTo(1L);
        assertThat(root.get("content").get(0).get("name").asText()).isEqualTo("Laptop A");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/products?minPrice=200&maxPrice=100",
            "/api/products?sortBy=createdAt&sortDir=asc",
            "/api/products?sortBy=price&sortDir=up"
    })
    void list_withInvalidQuery_returnsBadRequest(String pathWithQuery) {
        String url = "http://localhost:" + port + pathWithQuery;

        ResponseEntity<String> response = rest.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody())).contains("invalid_argument");
    }

    @Test
    void list_sorting_byPriceDesc_returnsExpectedOrder() throws Exception {
        productRepository.save(new com.example.productservice.model.Product("Low", "d", new BigDecimal("10"), "seller-a", 3));
        productRepository.save(new com.example.productservice.model.Product("Mid", "d", new BigDecimal("25"), "seller-a", 3));
        productRepository.save(new com.example.productservice.model.Product("High", "d", new BigDecimal("99"), "seller-a", 3));

        String url = "http://localhost:" + port + "/api/products?sortBy=price&sortDir=desc&page=0&size=10";
        ResponseEntity<String> response = rest.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String body = Objects.requireNonNull(response.getBody(), "sort response body must not be null");
        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        com.fasterxml.jackson.databind.JsonNode content = root.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.get(0).get("name").asText()).isEqualTo("High");
        assertThat(content.get(1).get("name").asText()).isEqualTo("Mid");
        assertThat(content.get(2).get("name").asText()).isEqualTo("Low");
    }

    private void assertFilteredCount(String url, long expectedCount) throws Exception {
        ResponseEntity<String> response = rest.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = Objects.requireNonNull(response.getBody(), "filtered response body must not be null");
        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        assertThat(root.get("totalElements").asLong()).isEqualTo(expectedCount);
    }

    
}
