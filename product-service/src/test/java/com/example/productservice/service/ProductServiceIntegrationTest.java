package com.example.productservice.service;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Tag("integration")
public class ProductServiceIntegrationTest {

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

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_savesEntity() {
        ProductDto dto = new ProductDto(null, "X", "d", BigDecimal.valueOf(1.23));
        Product p = productService.createProduct(dto, "owner-1");
        assertThat(p).isNotNull();
        assertThat(p.getId()).isNotNull();
        assertThat(p.getOwnerId()).isEqualTo("owner-1");
    }

    @Test
    void updateProduct_allowsOwner_and_updates() {
        Product p = new Product("Old", "old", BigDecimal.valueOf(2.0), "owner-1");
        Product saved = productRepository.save(p);
        ProductDto dto = new ProductDto(null, "New", "new", BigDecimal.valueOf(3.0));
        Product updated = productService.updateProduct(saved.getId(), dto, "owner-1");
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("New");
    }

    @Test
    void updateProduct_forbidsNonOwner() {
        Product p = new Product("Old", "old", BigDecimal.valueOf(2.0), "owner-1");
        Product saved = productRepository.save(p);
        ProductDto dto = new ProductDto(null, "New", "new", BigDecimal.valueOf(3.0));
        Product updated = productService.updateProduct(saved.getId(), dto, "other-owner");
        assertThat(updated).isNull();
    }

    @Test
    void listProducts_delegatesToRepository_and_searches() {
        productRepository.save(new Product("Apple", "a", BigDecimal.ONE, "o1"));
        productRepository.save(new Product("Banana", "b", BigDecimal.valueOf(2), "o2"));
        var page = productService.listProducts(0, 10, null);
        assertThat(page.getTotalElements()).isEqualTo(2);

        var search = productService.listProducts(0, 10, "ap");
        assertThat(search.getTotalElements()).isEqualTo(1);
    }
}
