package com.example.orderservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MongoMigrationIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("APP_JWT_SECRET", () -> "test-secret-key-for-order-tests-32-characters");
    }

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

    @Test
    void shouldCreateRequiredIndexes() {
        Assertions.assertTrue(hasIndexWithFields("orders", "buyerId", "createdAt"),
                "Should create compound index on buyerId + createdAt for orders");
        Assertions.assertTrue(hasIndexWithFields("orders", "items.sellerId", "createdAt"),
                "Should create compound index on items.sellerId + createdAt for orders");
        Assertions.assertTrue(hasIndexWithFields("orders", "status", "createdAt"),
                "Should create compound index on status + createdAt for orders");

        Assertions.assertTrue(hasUniqueIndexOnField("shopping_carts", "userId"),
                "Should create unique index on shopping_carts.userId");
        Assertions.assertTrue(hasIndexWithFields("shopping_carts", "updatedAt"),
                "Should create TTL index on shopping_carts.updatedAt");

        Assertions.assertTrue(hasUniqueIndexOnField("wishlists", "userId"),
                "Should create unique index on wishlists.userId");

        Assertions.assertTrue(hasUniqueIndexOnField("user_profiles", "userId"),
                "Should create unique index on user_profiles.userId");
        Assertions.assertTrue(hasIndexWithFields("user_profiles", "totalOrders"),
                "Should create stats index on user_profiles.totalOrders");
        Assertions.assertTrue(hasIndexWithFields("user_profiles", "totalSpent"),
                "Should create stats index on user_profiles.totalSpent");

        Assertions.assertTrue(hasUniqueIndexOnField("seller_profiles", "sellerId"),
                "Should create unique index on seller_profiles.sellerId");
        Assertions.assertTrue(hasIndexWithFields("seller_profiles", "totalRevenue"),
                "Should create stats index on seller_profiles.totalRevenue");
        Assertions.assertTrue(hasIndexWithFields("seller_profiles", "averageRating"),
                "Should create stats index on seller_profiles.averageRating");
    }

    private boolean hasIndexWithFields(String collection, String... expectedFields) {
        List<String> expected = List.of(expectedFields);
        return mongoTemplate.indexOps(Objects.requireNonNull(collection))
                .getIndexInfo()
                .stream()
                .map(IndexInfo::getIndexFields)
                .map(fields -> fields.stream().map(IndexField::getKey).toList())
                .anyMatch(keys -> keys.equals(expected));
    }

    private boolean hasUniqueIndexOnField(String collection, String field) {
        return mongoTemplate.indexOps(Objects.requireNonNull(collection))
                .getIndexInfo()
                .stream()
                .anyMatch(index -> index.isUnique()
                        && index.getIndexFields().size() == 1
                        && index.getIndexFields().get(0).getKey().equals(field));
    }
}
