package com.example.productservice.kafka;

import com.example.productservice.repository.ProductRepository;
import com.example.shared.kafka.KafkaTopics;
import com.example.shared.kafka.event.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Consumer for user-related events.
 * When a user (seller) is deleted, this consumer deletes all their products.
 */
@Service
public class UserEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(UserEventConsumer.class);

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public UserEventConsumer(ProductRepository productRepository,
                             ProductEventProducer productEventProducer) {
        this.productRepository = productRepository;
        this.productEventProducer = productEventProducer;
    }

    /**
     * Handle user deletion events.
     * When a seller is deleted, we must delete all their products
     * and publish ProductDeletedEvents for media cleanup.
     *
     * @param event the user deleted event
     */
    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "product-service-group")
    public void handleUserDeleted(UserDeletedEvent event) {
        LOG.info("Received UserDeletedEvent: {}", event);

        String userId = event.getUserId();

        // Find all products owned by this user
        var products = productRepository.findByOwnerId(userId);

        if (products.isEmpty()) {
            LOG.info("No products found for user {}", userId);
            return;
        }

        LOG.info("Deleting {} products for user {}", products.size(), userId);

        // Delete each product and publish delete event for media cleanup
        for (var product : products) {
            productEventProducer.publishProductDeleted(product.getId(), userId);
            productRepository.deleteById(Objects.requireNonNull(product.getId()));
            LOG.info("Deleted product {} for user {}", product.getId(), userId);
        }

        LOG.info("Finished cleaning up products for user {}", userId);
    }
}
