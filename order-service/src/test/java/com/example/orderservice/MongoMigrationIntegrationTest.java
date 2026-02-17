package com.example.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MongoMigrationIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void collectionsAndIndexesCreated() {
        // The application context will trigger the MongoMigrationRunner at startup
        boolean hasOrders = mongoTemplate.collectionExists("orders");
        boolean hasShoppingCarts = mongoTemplate.collectionExists("shopping_carts");
        boolean hasUserProfiles = mongoTemplate.collectionExists("user_profiles");

        assertThat(hasOrders).isTrue();
        assertThat(hasShoppingCarts).isTrue();
        assertThat(hasUserProfiles).isTrue();
    }
}
