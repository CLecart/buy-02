package com.example.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SellerProfile entity representing extended profile information for sellers.
 * Stored in MongoDB collection "seller_profiles".
 */
@Document(collection = "seller_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sellerId;

    private String storeName;

    private String storeDescription;

    private String phone;

    private String businessAddress;

    private String city;

    private String postalCode;

    private String country;

    private String businessLicense;

    private Integer totalProductsSold;

    private BigDecimal totalRevenue;

    private BigDecimal averageOrderValue;

    private Double averageRating;

    private Integer totalReviews;

    private List<String> bestSellingProductIds;

    private List<String> topRatedProductIds;

    private Map<String, Integer> soldProductCounts;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastOrderDate;

    private String logoMediaId;

    private String bannerMediaId;

    private Boolean verified;
}
