package com.example.shared.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ShoppingCart responses.
 * Used when returning cart information to clients.
 */
public record ShoppingCartDTO(
        String id,

        @NotBlank(message = "User ID is required")
        String userId,

        @Valid
        List<CartItemDTO> items,

        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.00", message = "Total price cannot be negative")
        BigDecimal totalPrice,

        @NotNull(message = "Item count is required")
        @Min(value = 0, message = "Item count cannot be negative")
        Integer itemCount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    /**
     * DTO for CartItem embedded in ShoppingCart.
     */
    public record CartItemDTO(
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
            BigDecimal subtotal,

            LocalDateTime createdAt
    ) {}
}
