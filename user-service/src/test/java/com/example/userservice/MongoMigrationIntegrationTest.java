package com.example.userservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;

@SpringBootTest
@Testcontainers
class MongoMigrationIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("APP_JWT_SECRET", () -> "test-secret-key-for-user-tests-32-characters");
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void collectionsAndIndexesCreated() {
        Assertions.assertTrue(mongoTemplate.collectionExists("users"),
            "Should create users collection");
        Assertions.assertTrue(mongoTemplate.collectionExists("user_profiles"),
            "Should create user_profiles collection");

        Assertions.assertTrue(hasUniqueIndexOnField("users", "email"),
            "Should create unique index on users.email");
        Assertions.assertTrue(hasUniqueIndexOnField("user_profiles", "userId"),
            "Should create unique index on user_profiles.userId");
        Assertions.assertTrue(hasIndexWithFields("user_profiles", "totalOrders"),
            "Should create stats index on user_profiles.totalOrders");
        Assertions.assertTrue(hasIndexWithFields("user_profiles", "totalSpent"),
            "Should create stats index on user_profiles.totalSpent");
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
