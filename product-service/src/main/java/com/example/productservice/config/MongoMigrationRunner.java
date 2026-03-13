package com.example.productservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MongoMigrationRunner {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION_PRODUCTS = "products";
    private static final String COLLECTION_SELLER_PROFILES = "seller_profiles";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String COLLECTION_WISHLISTS = "wishlists";
    private static final String FIELD_AVERAGE_RATING = "averageRating";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_OWNER_ID = "ownerId";
    private static final String FIELD_SELLER_ID = "sellerId";
    private static final String FIELD_TOTAL_ORDERS = "totalOrders";
    private static final String FIELD_TOTAL_REVENUE = "totalRevenue";
    private static final String FIELD_TOTAL_SPENT = "totalSpent";
    private static final String FIELD_USER_ID_CAMEL = "userId";
    private static final String FIELD_VERIFIED = "verified";

    public MongoMigrationRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        createCollectionIfMissing(COLLECTION_PRODUCTS);
        createCollectionIfMissing(COLLECTION_SELLER_PROFILES);
        createCollectionIfMissing(COLLECTION_USER_PROFILES);
        createCollectionIfMissing(COLLECTION_WISHLISTS);

        mongoTemplate.indexOps(COLLECTION_PRODUCTS).ensureIndex(new Index().on(FIELD_OWNER_ID, org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_PRODUCTS).ensureIndex(new Index().on(FIELD_CATEGORY, org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps(COLLECTION_USER_PROFILES).ensureIndex(new Index().on(FIELD_USER_ID_CAMEL, org.springframework.data.domain.Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(COLLECTION_USER_PROFILES).ensureIndex(new Index().on(FIELD_TOTAL_ORDERS, org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_USER_PROFILES).ensureIndex(new Index().on(FIELD_TOTAL_SPENT, org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps(COLLECTION_SELLER_PROFILES).ensureIndex(new Index().on(FIELD_SELLER_ID, org.springframework.data.domain.Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(COLLECTION_SELLER_PROFILES).ensureIndex(new Index().on(FIELD_TOTAL_REVENUE, org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_SELLER_PROFILES).ensureIndex(new Index().on(FIELD_AVERAGE_RATING, org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_SELLER_PROFILES).ensureIndex(new Index().on(FIELD_VERIFIED, org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps(COLLECTION_WISHLISTS).ensureIndex(new Index().on(FIELD_USER_ID_CAMEL, org.springframework.data.domain.Sort.Direction.ASC).unique());
    }

    private void createCollectionIfMissing(String name) {
        Objects.requireNonNull(name);
        if (!mongoTemplate.collectionExists(name)) {
            mongoTemplate.createCollection(name);
        }
    }
}
