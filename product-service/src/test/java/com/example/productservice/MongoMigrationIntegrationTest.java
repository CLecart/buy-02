package com.example.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MongoMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void collectionsAndIndexesCreated() {
        assertThat(mongoTemplate.collectionExists("products")).isTrue();
        assertThat(mongoTemplate.collectionExists("user_profiles")).isTrue();
        assertThat(mongoTemplate.collectionExists("seller_profiles")).isTrue();
        assertThat(mongoTemplate.collectionExists("wishlists")).isTrue();

        assertThat(hasIndexWithFields("products", "ownerId")).isTrue();
        assertThat(hasIndexWithFields("products", "category")).isTrue();

        assertThat(hasUniqueIndexOnField("user_profiles", "userId")).isTrue();
        assertThat(hasIndexWithFields("user_profiles", "totalOrders")).isTrue();
        assertThat(hasIndexWithFields("user_profiles", "totalSpent")).isTrue();

        assertThat(hasUniqueIndexOnField("seller_profiles", "sellerId")).isTrue();
        assertThat(hasIndexWithFields("seller_profiles", "totalRevenue")).isTrue();
        assertThat(hasIndexWithFields("seller_profiles", "averageRating")).isTrue();
        assertThat(hasIndexWithFields("seller_profiles", "verified")).isTrue();

        assertThat(hasUniqueIndexOnField("wishlists", "userId")).isTrue();
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