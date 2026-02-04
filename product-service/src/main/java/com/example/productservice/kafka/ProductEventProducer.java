package com.example.productservice.kafka;

import com.example.shared.kafka.KafkaTopics;
import com.example.shared.kafka.event.ProductCreatedEvent;
import com.example.shared.kafka.event.ProductDeletedEvent;
import com.example.shared.kafka.event.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Service responsible for publishing product-related events to Kafka.
 * Media-service listens to these events for media cleanup operations.
 */
@Service
public class ProductEventProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ProductEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a product created event.
     *
     * @param productId   the ID of the created product
     * @param sellerId    the ID of the seller who created the product
     * @param name        product name
     * @param description product description
     * @param price       product price
     * @param quantity    product quantity
     */
    public void publishProductCreated(String productId, String sellerId, String name,
                                       String description, BigDecimal price, Integer quantity) {
        var event = new ProductCreatedEvent(productId, sellerId, name, description, price, quantity);
        LOG.info("Publishing ProductCreatedEvent: {}", event);
        kafkaTemplate.send(KafkaTopics.PRODUCT_EVENTS, Objects.requireNonNull(productId), event);
    }

    /**
     * Publish a product updated event.
     *
     * @param productId   the ID of the updated product
     * @param sellerId    the ID of the seller who owns the product
     * @param name        updated product name
     * @param description updated product description
     * @param price       updated product price
     * @param quantity    updated product quantity
     */
    public void publishProductUpdated(String productId, String sellerId, String name,
                                       String description, BigDecimal price, Integer quantity) {
        var event = new ProductUpdatedEvent(productId, sellerId, name, description, price, quantity);
        LOG.info("Publishing ProductUpdatedEvent: {}", event);
        kafkaTemplate.send(KafkaTopics.PRODUCT_EVENTS, Objects.requireNonNull(productId), event);
    }

    /**
     * Publish a product deleted event.
     * Media-service consumes this to cleanup orphan media files.
     *
     * @param productId the ID of the deleted product
     * @param sellerId  the ID of the seller who owned the product
     */
    public void publishProductDeleted(String productId, String sellerId) {
        var event = new ProductDeletedEvent(productId, sellerId);
        LOG.info("Publishing ProductDeletedEvent: {}", event);
        kafkaTemplate.send(KafkaTopics.PRODUCT_EVENTS, Objects.requireNonNull(productId), event);
    }
}
