package com.example.shared.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * UserProfile entity representing extended profile information for customers.
 * Stored in MongoDB collection "user_profiles".
 */
@Document(collection = "user_profiles")
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastOrderDate;

    private String avatarMediaId;

    // Constructors
    public UserProfile() {
    }

    public UserProfile(String userId) {
        this.userId = userId;
        this.totalOrders = 0;
        this.totalSpent = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
        updateAverageOrderValue();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public List<String> getFavoriteProductIds() {
        return favoriteProductIds;
    }

    public void setFavoriteProductIds(List<String> favoriteProductIds) {
        this.favoriteProductIds = favoriteProductIds;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getMostPurchasedProductIds() {
        return mostPurchasedProductIds;
    }

    public void setMostPurchasedProductIds(List<String> mostPurchasedProductIds) {
        this.mostPurchasedProductIds = mostPurchasedProductIds;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public String getAvatarMediaId() {
        return avatarMediaId;
    }

    public void setAvatarMediaId(String avatarMediaId) {
        this.avatarMediaId = avatarMediaId;
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
                ", createdAt=" + createdAt +
                '}';
    }
}
