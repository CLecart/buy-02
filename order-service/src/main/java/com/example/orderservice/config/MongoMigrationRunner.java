package com.example.orderservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Lightweight MongoDB migration runner that ensures required collections and indexes exist.
 * This approach avoids adding a third-party migration framework while ensuring schema
 * artifacts required by the project are created on startup in development/test environments.
 */
@Component
public class MongoMigrationRunner {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION_CARTS = "shopping_carts";
    private static final String COLLECTION_ORDERS = "orders";
    // order items are embedded in orders; no dedicated collection
    private static final String COLLECTION_WISHLISTS = "wishlists";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String COLLECTION_SELLER_PROFILES = "seller_profiles";

    // Field names must match the document property names used in the model classes (camelCase)
    private static final String FIELD_BUYER_ID = "buyerId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_TOTAL_SPENT = "totalSpent";
    private static final String FIELD_TOTAL_REVENUE = "totalRevenue";

    public MongoMigrationRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        // Create collections if they don't exist
        createCollectionIfMissing(COLLECTION_CARTS);
        createCollectionIfMissing(COLLECTION_ORDERS);
        createCollectionIfMissing(COLLECTION_WISHLISTS);
        createCollectionIfMissing(COLLECTION_USER_PROFILES);
        createCollectionIfMissing(COLLECTION_SELLER_PROFILES);

        // Indexes for orders
        mongoTemplate.indexOps(COLLECTION_ORDERS).ensureIndex(new Index().on(FIELD_BUYER_ID, org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_ORDERS).ensureIndex(new Index().on(FIELD_STATUS, org.springframework.data.domain.Sort.Direction.ASC));

        // Indexes for carts and wishlist
        // Ensure unique index on cart owner
        mongoTemplate.indexOps(COLLECTION_CARTS).ensureIndex(new Index().on(FIELD_USER_ID, org.springframework.data.domain.Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(COLLECTION_WISHLISTS).ensureIndex(new Index().on(FIELD_USER_ID, org.springframework.data.domain.Sort.Direction.ASC));

        // Indexes for profiles
        mongoTemplate.indexOps(COLLECTION_USER_PROFILES).ensureIndex(new Index().on(FIELD_TOTAL_SPENT, org.springframework.data.domain.Sort.Direction.DESC));
        mongoTemplate.indexOps(COLLECTION_SELLER_PROFILES).ensureIndex(new Index().on(FIELD_TOTAL_REVENUE, org.springframework.data.domain.Sort.Direction.DESC));
    }

    private void createCollectionIfMissing(String name) {
        Objects.requireNonNull(name);
        if (!mongoTemplate.collectionExists(name)) {
            mongoTemplate.createCollection(name);
        }
    }
}
