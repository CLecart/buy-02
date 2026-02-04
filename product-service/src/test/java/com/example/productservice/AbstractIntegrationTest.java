package com.example.productservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require MongoDB and Kafka.
 * Uses Testcontainers to spin up real instances for testing.
 */
public abstract class AbstractIntegrationTest {

    protected static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(
            DockerImageName.parse("mongo:6.0")
    );

    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    static {
        // Start containers in parallel
        Startables.deepStart(MONGO_CONTAINER, KAFKA_CONTAINER).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MongoDB
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);

        // Kafka - enable listeners for integration tests with real Kafka
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.listener.auto-startup", () -> "true");

        // JWT secret for tests
        registry.add("APP_JWT_SECRET", () -> "test-secret-key-for-jwt-signing-minimum-32-characters");
    }
}
