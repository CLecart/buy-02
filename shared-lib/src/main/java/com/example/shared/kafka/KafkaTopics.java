package com.example.shared.kafka;

/**
 * Central definition of all Kafka topic names used across services.
 * Ensures consistency and prevents typos in topic references.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class, prevent instantiation
    }

    /**
     * Topic for product lifecycle events (created, updated, deleted).
     * - Producer: product-service
     * - Consumers: media-service (cleanup on delete)
     */
    public static final String PRODUCT_EVENTS = "product-events";

    /**
     * Topic for user lifecycle events (deleted).
     * - Producer: user-service
     * - Consumers: product-service (delete user's products), media-service (cleanup media)
     */
    public static final String USER_EVENTS = "user-events";

    /**
     * Topic for media-related events.
     * Reserved for future use (e.g., media processing completion).
     */
    public static final String MEDIA_EVENTS = "media-events";
}
