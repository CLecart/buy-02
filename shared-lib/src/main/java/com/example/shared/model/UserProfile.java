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
import java.util.HashMap;

/**
 * UserProfile entity representing extended profile information for customers.
 * Stored in MongoDB collection "user_profiles".
 */
@Document(collection = "user_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String firstName;

    private String lastName;

    private String phone;

    private String address;

    private String city;

    private String postalCode;

    private String country;

    private Integer totalOrders;

    private BigDecimal totalSpent;

    private BigDecimal averageOrderValue;

    private List<String> favoriteProductIds;

    private List<String> mostPurchasedProductIds;

    private Map<String, Integer> purchasedProductCounts;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastOrderDate;

    private String avatarMediaId;

    // Constructors
    public UserProfile(String userId) {
        this.userId = userId;
        this.totalOrders = 0;
        this.totalSpent = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
        this.purchasedProductCounts = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    // Business methods
    public void recordNewOrder(BigDecimal orderTotal) {
        this.totalOrders = (this.totalOrders != null) ? this.totalOrders + 1 : 1;
        this.totalSpent = (this.totalSpent != null) ? this.totalSpent.add(orderTotal) : orderTotal;
        this.lastOrderDate = LocalDateTime.now();
        updateAverageOrderValue();
        this.updatedAt = LocalDateTime.now();
    }

    private void updateAverageOrderValue() {
        if (this.totalOrders != null && this.totalOrders > 0 && this.totalSpent != null) {
            this.averageOrderValue = this.totalSpent.divide(
                new BigDecimal(this.totalOrders),
                2,
                java.math.RoundingMode.HALF_UP
            );
        }
    }

    public String getFullName() {
        String first = (this.firstName != null) ? this.firstName : "";
        String last = (this.lastName != null) ? this.lastName : "";
        return (first + " " + last).trim();
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", totalOrders=" + totalOrders +
                ", totalSpent=" + totalSpent +
                ", lastOrderDate=" + lastOrderDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
