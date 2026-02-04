package com.example.shared.kafka;

import com.example.shared.event.OrderCreatedEvent;
import com.example.shared.event.OrderStatusChangedEvent;
import com.example.shared.event.CartUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
        log.info("Publishing OrderCreatedEvent: {}", event.orderId());
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId(), event);
    }

    /**
     * Publish order status changed event.
     * Triggers notifications and profile updates.
     */
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Publishing OrderStatusChangedEvent: {} -> {}", event.orderId(), event.newStatus());
        kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, event.orderId(), event);
    }

    /**
     * Publish cart updated event.
     * Used for inventory tracking and analytics.
     */
    public void publishCartUpdated(CartUpdatedEvent event) {
        log.debug("Publishing CartUpdatedEvent: {} - {}", event.cartId(), event.action());
        kafkaTemplate.send(CART_UPDATED_TOPIC, event.cartId(), event);
    }
}
