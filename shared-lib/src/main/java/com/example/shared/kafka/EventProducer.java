package com.example.shared.kafka;

import com.example.shared.event.OrderCreatedEvent;
import com.example.shared.event.OrderStatusChangedEvent;
import com.example.shared.event.CartUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Kafka producer for publishing domain events.
 * Enables event-driven architecture across microservices.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_STATUS_CHANGED_TOPIC = "order-status-changed";
    private static final String CART_UPDATED_TOPIC = "cart-updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish order created event.
     * Triggers updates to user and seller profiles.
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        String orderId = Objects.requireNonNull(event.orderId(), "orderId");
        log.info("Publishing OrderCreatedEvent: {}", orderId);
        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId, event);
    }

    /**
     * Publish order status changed event.
     * Triggers notifications and profile updates.
     */
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        String orderId = Objects.requireNonNull(event.orderId(), "orderId");
        log.info("Publishing OrderStatusChangedEvent: {} -> {}", orderId, event.newStatus());
        kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, orderId, event);
    }

    /**
     * Publish cart updated event.
     * Used for inventory tracking and analytics.
     */
    public void publishCartUpdated(CartUpdatedEvent event) {
        String cartId = Objects.requireNonNull(event.cartId(), "cartId");
        log.debug("Publishing CartUpdatedEvent: {} - {}", cartId, event.action());
        kafkaTemplate.send(CART_UPDATED_TOPIC, cartId, event);
    }
}
