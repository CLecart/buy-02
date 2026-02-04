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

    private String description;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastOrderDate;

    private String logoMediaId;

    private String bannerMediaId;

    private Boolean isActive;

    // Constructors
    public SellerProfile() {
    }

    public SellerProfile(String sellerId) {
        this.sellerId = sellerId;
        this.totalProductsSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
        this.averageRating = 0.0;
        this.totalReviews = 0;
        this.isActive = true;
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

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
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

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTotalProductsSold() {
        return totalProductsSold;
    }

    public void setTotalProductsSold(Integer totalProductsSold) {
        this.totalProductsSold = totalProductsSold;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
        updateAverageOrderValue();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getBestSellingProductIds() {
        return bestSellingProductIds;
    }

    public void setBestSellingProductIds(List<String> bestSellingProductIds) {
        this.bestSellingProductIds = bestSellingProductIds;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getTopRatedProductIds() {
        return topRatedProductIds;
    }

    public void setTopRatedProductIds(List<String> topRatedProductIds) {
        this.topRatedProductIds = topRatedProductIds;
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

    public String getLogoMediaId() {
        return logoMediaId;
    }

    public void setLogoMediaId(String logoMediaId) {
        this.logoMediaId = logoMediaId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBannerMediaId() {
        return bannerMediaId;
    }

    public void setBannerMediaId(String bannerMediaId) {
        this.bannerMediaId = bannerMediaId;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void recordNewSale(Integer quantity, BigDecimal revenue) {
        this.totalProductsSold = (this.totalProductsSold != null) ? this.totalProductsSold + quantity : quantity;
        this.totalRevenue = (this.totalRevenue != null) ? this.totalRevenue.add(revenue) : revenue;
        this.lastOrderDate = LocalDateTime.now();
        updateAverageOrderValue();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Double newRating) {
        // Simple average calculation (could be weighted in production)
        this.averageRating = newRating;
        this.updatedAt = LocalDateTime.now();
    }

    private void updateAverageOrderValue() {
        if (this.totalProductsSold != null && this.totalProductsSold > 0 && this.totalRevenue != null) {
            this.averageOrderValue = this.totalRevenue.divide(
                new BigDecimal(this.totalProductsSold),
                2,
                java.math.RoundingMode.HALF_UP
            );
        }
    }

    @Override
    public String toString() {
        return "SellerProfile{" +
                "sellerId='" + sellerId + '\'' +
                ", storeName='" + storeName + '\'' +
                ", totalProductsSold=" + totalProductsSold +
                ", totalRevenue=" + totalRevenue +
                ", averageRating=" + averageRating +
                ", createdAt=" + createdAt +
                '}';
    }
}
