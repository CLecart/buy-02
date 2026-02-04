package com.example.shared.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when a new order is created.
 * Triggers updates to user and seller profiles.
 */
public record OrderCreatedEvent(
        String orderId,
        String buyerId,
        String buyerEmail,
        List<OrderItemSnapshot> items,
        BigDecimal totalPrice,
        String shippingAddress,
        LocalDateTime createdAt
) implements Serializable {

    public record OrderItemSnapshot(
            String productId,
            String sellerId,
            String productName,
            Integer quantity,
            BigDecimal price,
            BigDecimal subtotal
    ) implements Serializable {}
}
