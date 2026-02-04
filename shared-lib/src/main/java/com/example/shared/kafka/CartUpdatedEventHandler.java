package com.example.shared.kafka;

import com.example.shared.event.CartUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that handles CartUpdatedEvent.
 * Tracks cart activity for analytics and inventory management.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartUpdatedEventHandler {

    @KafkaListener(
        topics = "cart-updated",
        groupId = "analytics-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCartUpdated(CartUpdatedEvent event) {
        log.debug("Handling CartUpdatedEvent: {} - {}", event.cartId(), event.action());

        try {
            switch (event.action()) {
                case "ITEM_ADDED":
                    handleItemAdded(event);
                    break;
                case "ITEM_REMOVED":
                    handleItemRemoved(event);
                    break;
                case "QUANTITY_CHANGED":
                    handleQuantityChanged(event);
                    break;
                case "CLEARED":
                    handleCleared(event);
                    break;
                default:
                    log.warn("Unknown cart action: {}", event.action());
            }

            log.debug("CartUpdatedEvent processed: {}", event.cartId());
        } catch (Exception e) {
            log.error("Error processing CartUpdatedEvent: {}", event.cartId(), e);
            // Don't throw - analytics events shouldn't block transactions
        }
    }

    private void handleItemAdded(CartUpdatedEvent event) {
        log.debug("Item added to cart: product={}, quantity={}", event.productId(), event.quantity());
        // TODO: Track product interest for recommendations
        // TODO: Update product view statistics
    }

    private void handleItemRemoved(CartUpdatedEvent event) {
        log.debug("Item removed from cart: product={}", event.productId());
        // TODO: Track abandoned items for retargeting
    }

    private void handleQuantityChanged(CartUpdatedEvent event) {
        log.debug("Item quantity changed: product={}, quantity={}", event.productId(), event.quantity());
        // TODO: Update inventory reservation
    }

    private void handleCleared(CartUpdatedEvent event) {
        log.debug("Cart cleared: cart={}", event.cartId());
        // TODO: Track abandoned carts for recovery campaigns
    }
}
