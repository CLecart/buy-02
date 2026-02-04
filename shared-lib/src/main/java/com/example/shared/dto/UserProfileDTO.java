package com.example.shared.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for UserProfile responses.
 * Used when returning customer profile information.
 */
public record UserProfileDTO(
        String id,

        @NotBlank(message = "User ID is required")
        String userId,

        @Min(value = 0, message = "Total orders cannot be negative")
        Integer totalOrders,

        @DecimalMin(value = "0.00", message = "Total spent cannot be negative")
        BigDecimal totalSpent,

        @DecimalMin(value = "0.00", message = "Average order value cannot be negative")
        BigDecimal averageOrderValue,

        List<String> favoriteProductIds,

        List<String> mostPurchasedProductIds,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}
