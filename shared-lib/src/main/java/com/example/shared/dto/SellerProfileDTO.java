package com.example.shared.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for SellerProfile responses.
 * Used when returning seller profile information.
 */
public record SellerProfileDTO(
        String id,

        @NotBlank(message = "Seller ID is required")
        String sellerId,

        @NotBlank(message = "Store name is required")
        @Size(max = 200, message = "Store name must not exceed 200 characters")
        String storeName,

        @Size(max = 1000, message = "Store description must not exceed 1000 characters")
        String storeDescription,

        @Min(value = 0, message = "Total products sold cannot be negative")
        Integer totalProductsSold,

        @DecimalMin(value = "0.00", message = "Total revenue cannot be negative")
        BigDecimal totalRevenue,

        @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
        @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
        Double averageRating,

        @Min(value = 0, message = "Total reviews cannot be negative")
        Integer totalReviews,

        List<String> bestSellingProductIds,

        Boolean verified,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}
