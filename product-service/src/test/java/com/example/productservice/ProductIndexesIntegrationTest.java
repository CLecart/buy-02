package com.example.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.Indexed;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductIndexesIntegrationTest {

    @Test
    void productOwnerFieldIsAnnotatedIndexed() throws NoSuchFieldException {
        boolean annotated = com.example.productservice.model.Product.class
                .getDeclaredField("ownerId")
                .isAnnotationPresent(Indexed.class);
        assertTrue(annotated, "Expected Product.ownerId to be annotated with @Indexed");
    }
}
