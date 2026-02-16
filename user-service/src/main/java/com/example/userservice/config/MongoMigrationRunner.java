package com.example.userservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MongoMigrationRunner {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String FIELD_USER_ID = "user_id";

    public MongoMigrationRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        createCollectionIfMissing(COLLECTION_USERS);
        createCollectionIfMissing(COLLECTION_USER_PROFILES);

        mongoTemplate.indexOps(COLLECTION_USERS).ensureIndex(new Index().on("email", org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION_USER_PROFILES).ensureIndex(new Index().on(FIELD_USER_ID, org.springframework.data.domain.Sort.Direction.ASC));
    }

    private void createCollectionIfMissing(String name) {
        Objects.requireNonNull(name);
        if (!mongoTemplate.collectionExists(name)) {
            mongoTemplate.createCollection(name);
        }
    }
}
