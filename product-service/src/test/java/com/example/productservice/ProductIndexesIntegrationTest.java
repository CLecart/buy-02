package com.example.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.index.Indexed;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProductIndexesIntegrationTest {

    static String TEST_JWT_SECRET;

    @org.springframework.test.context.DynamicPropertySource
    static void setProps(org.springframework.test.context.DynamicPropertyRegistry reg) {
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        TEST_JWT_SECRET = java.util.HexFormat.of().formatHex(secretBytes);
        reg.add("APP_JWT_SECRET", () -> TEST_JWT_SECRET);
    }

    @Test
    void productOwnerFieldIsAnnotatedIndexed() throws NoSuchFieldException {
        boolean annotated = com.example.productservice.model.Product.class
                .getDeclaredField("ownerId")
                .isAnnotationPresent(Indexed.class);
        assertTrue(annotated, "Expected Product.ownerId to be annotated with @Indexed");
    }
}
