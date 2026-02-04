package com.example.shared.dto;

import com.example.shared.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating order status.
 * Used by sellers/admins to update order state.
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status,

        String trackingNumber
) {}
