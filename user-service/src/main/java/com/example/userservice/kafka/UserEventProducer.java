package com.example.userservice.kafka;

import com.example.shared.kafka.KafkaTopics;
import com.example.shared.kafka.event.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service responsible for publishing user-related events to Kafka.
 * Product-service and media-service listen to these events for cascade deletion.
 */
@Service
public class UserEventProducer {

    private static final Logger LOG = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a user deleted event.
     * Product-service consumes this to delete user's products.
     * Media-service consumes this to cleanup user's media files.
     *
     * @param userId   the ID of the deleted user
     * @param userRole the role of the deleted user (SELLER, CLIENT)
     */
    public void publishUserDeleted(String userId, String userRole) {
        var event = new UserDeletedEvent(userId, userRole);
        LOG.info("Publishing UserDeletedEvent: {}", event);
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, Objects.requireNonNull(userId), event);
    }
}
