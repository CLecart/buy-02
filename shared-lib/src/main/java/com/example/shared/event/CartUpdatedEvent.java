package com.example.shared.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when shopping cart is updated.
 * Used for inventory tracking and analytics.
 */
public record CartUpdatedEvent(
        String cartId,
        String userId,
        String action, // "ITEM_ADDED", "ITEM_REMOVED", "QUANTITY_CHANGED", "CLEARED"
        String productId,
        Integer quantity,
        BigDecimal price,
        Integer totalItems,
        BigDecimal totalPrice,
        LocalDateTime updatedAt
) implements Serializable {}
