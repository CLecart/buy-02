package com.example.shared.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for updating cart item quantity.
 */
public record UpdateCartItemRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity must not exceed 999")
        Integer quantity
) {}
