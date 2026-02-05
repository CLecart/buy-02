package com.example.mediaservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
 
import org.springframework.data.mongodb.core.index.Indexed;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MediaIndexesIntegrationTest {

    static String testJwtSecret;

    @org.springframework.test.context.DynamicPropertySource
    static void setProps(org.springframework.test.context.DynamicPropertyRegistry reg) {
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        testJwtSecret = java.util.HexFormat.of().formatHex(secretBytes);
        reg.add("APP_JWT_SECRET", () -> testJwtSecret);
    }

    @Test
    void mediaFieldsAreAnnotatedIndexed() throws NoSuchFieldException {
        boolean productIndexed = com.example.mediaservice.model.MediaFile.class
                .getDeclaredField("productId")
                .isAnnotationPresent(Indexed.class);
        boolean ownerIndexed = com.example.mediaservice.model.MediaFile.class
                .getDeclaredField("ownerId")
                .isAnnotationPresent(Indexed.class);

        assertTrue(productIndexed, "Expected MediaFile.productId to be annotated with @Indexed");
        assertTrue(ownerIndexed, "Expected MediaFile.ownerId to be annotated with @Indexed");
    }
}
