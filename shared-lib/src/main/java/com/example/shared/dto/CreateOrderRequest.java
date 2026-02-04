package com.example.shared.dto;

import com.example.shared.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a new order.
 * Used when submitting an order from shopping cart or direct purchase.
 */
public record CreateOrderRequest(
        @NotBlank(message = "Buyer ID is required")
        String buyerId,

        @NotBlank(message = "Buyer email is required")
        @Email(message = "Buyer email must be valid")
        String buyerEmail,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Shipping address must not exceed 500 characters")
        String shippingAddress
) {
    /**
     * DTO for an item in order creation request.
     */
    public record OrderItemRequest(
            @NotBlank(message = "Product ID is required")
            String productId,

            @NotBlank(message = "Seller ID is required")
            String sellerId,

            @NotBlank(message = "Product name is required")
            @Size(max = 200, message = "Product name must not exceed 200 characters")
            String productName,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 999, message = "Quantity must not exceed 999")
            Integer quantity,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.01", message = "Price must be positive")
            @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
            BigDecimal price
    ) {}
}
