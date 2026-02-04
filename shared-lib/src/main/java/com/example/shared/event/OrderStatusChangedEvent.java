package com.example.shared.event;

import com.example.shared.model.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when order status changes.
 * Triggers notifications and seller/buyer updates.
 */
public record OrderStatusChangedEvent(
        String orderId,
        String buyerId,
        String buyerEmail,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String reason,
        LocalDateTime changedAt
) implements Serializable {}
