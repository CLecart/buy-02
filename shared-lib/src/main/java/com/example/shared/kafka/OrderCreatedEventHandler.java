package com.example.shared.kafka;

import com.example.shared.event.OrderCreatedEvent;
import com.example.shared.exception.EventProcessingException;
import com.example.shared.service.SellerProfileService;
import com.example.shared.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that handles OrderCreatedEvent.
 * Updates seller and user profiles when new orders are created.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventHandler {

    private final SellerProfileService sellerProfileService;
    private final UserProfileService userProfileService;

    @KafkaListener(
        topics = "order-created",
        groupId = "profile-update-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent: {}", event.orderId());

        try {
            // Update user profile with new order
            userProfileService.recordNewOrder(event.buyerId(), event.totalPrice());

            // Update seller profiles with new sales
            event.items().forEach(item ->
                sellerProfileService.recordSale(
                    item.sellerId(),
                    item.quantity(),
                    item.subtotal()
                )
            );

            log.info("OrderCreatedEvent processed successfully: {}", event.orderId());
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", event.orderId(), e);
            throw new EventProcessingException("Failed to process order created event", e);
        }
    }
}
