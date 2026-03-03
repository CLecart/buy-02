package com.example.productservice;

import com.example.shared.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        createRequiredTopics();
    }

    private static void createRequiredTopics() {
        try (AdminClient adminClient = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers())
        )) {
            List<NewTopic> topics = List.of(
                    new NewTopic(KafkaTopics.PRODUCT_EVENTS, 1, (short) 1),
                    new NewTopic(KafkaTopics.USER_EVENTS, 1, (short) 1)
            );
            adminClient.createTopics(topics).all().get();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new IllegalStateException("Failed to create Kafka topics for integration tests", e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Kafka topics for integration tests", e);
        }
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
