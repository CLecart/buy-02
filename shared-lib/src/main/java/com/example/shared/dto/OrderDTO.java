package com.example.shared.dto;

import com.example.shared.model.OrderStatus;
import com.example.shared.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Order responses.
 * Used when returning order information to clients.
 */
public record OrderDTO(
        String id,

        @NotBlank(message = "Buyer ID is required")
        String buyerId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemDTO> items,

        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.01", message = "Total price must be positive")
        BigDecimal totalPrice,

        @NotNull(message = "Order status is required")
        OrderStatus status,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Shipping address must not exceed 500 characters")
        String shippingAddress,

        String trackingNumber,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    /**
     * DTO for OrderItem embedded in Order.
     */
    public record OrderItemDTO(
            @NotBlank(message = "Product ID is required")
            String productId,

            @NotBlank(message = "Seller ID is required")
            String sellerId,

            @NotBlank(message = "Product name is required")
            @Size(max = 200, message = "Product name must not exceed 200 characters")
            String productName,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            Integer quantity,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.01", message = "Price must be positive")
            BigDecimal price,

            @NotNull(message = "Subtotal is required")
            @DecimalMin(value = "0.01", message = "Subtotal must be positive")
            BigDecimal subtotal
    ) {}
}
