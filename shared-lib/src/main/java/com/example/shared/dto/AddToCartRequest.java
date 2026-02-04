package com.example.shared.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for adding an item to shopping cart.
 */
public record AddToCartRequest(
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
