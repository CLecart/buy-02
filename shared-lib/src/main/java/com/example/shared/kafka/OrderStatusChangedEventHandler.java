package com.example.shared.kafka;

import com.example.shared.event.OrderStatusChangedEvent;
import com.example.shared.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that handles OrderStatusChangedEvent.
 * Triggers notifications and processes status-specific actions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangedEventHandler {

    @KafkaListener(
        topics = "order-status-changed",
        groupId = "status-change-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Handling OrderStatusChangedEvent: {} - {} to {}",
            event.orderId(), event.oldStatus(), event.newStatus());

        try {
            switch (event.newStatus()) {
                case SHIPPED:
                    handleShipped(event);
                    break;
                case DELIVERED:
                    handleDelivered(event);
                    break;
                case CANCELLED:
                    handleCancelled(event);
                    break;
                default:
                    log.debug("No special handling for status: {}", event.newStatus());
            }

            log.info("OrderStatusChangedEvent processed: {}", event.orderId());
        } catch (Exception e) {
            log.error("Error processing OrderStatusChangedEvent: {}", event.orderId(), e);
            throw new EventProcessingException("Failed to process order status changed event", e);
        }
    }

    private void handleShipped(OrderStatusChangedEvent event) {
        log.info("Order shipped: {}", event.orderId());
        // Future: Send shipping notification email
        // Future: Update inventory tracking
    }

    private void handleDelivered(OrderStatusChangedEvent event) {
        log.info("Order delivered: {}", event.orderId());
        // Future: Send delivery confirmation email
        // Future: Trigger rating/review request
    }

    private void handleCancelled(OrderStatusChangedEvent event) {
        log.info("Order cancelled: {} - Reason: {}", event.orderId(), event.reason());
        // Future: Process refund
        // Future: Restore inventory
        // Future: Send cancellation notification
    }
}
