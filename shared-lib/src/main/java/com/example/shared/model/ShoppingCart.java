package com.example.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * ShoppingCart entity representing a user's shopping cart.
 * Stored in MongoDB collection "shopping_carts".
 * One cart per user, updated frequently.
 */
@Document(collection = "shopping_carts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<CartItem> items;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastModifiedAt;

    // Constructors
    public ShoppingCart() {
    }

    public ShoppingCart(String userId) {
        this.userId = userId;
        this.totalPrice = BigDecimal.ZERO;
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

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        recalculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    // Business methods
    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new java.util.ArrayList<>();
        }
        
        // Check if item already exists
        CartItem existing = this.items.stream()
            .filter(i -> i.getProductId().equals(item.getProductId()))
            .findFirst()
            .orElse(null);
        
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            this.items.add(item);
        }
        
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        recalculateTotalPrice();
    }

    public void removeItem(String productId) {
        if (this.items != null) {
            this.items.removeIf(item -> item.getProductId().equals(productId));
            this.updatedAt = LocalDateTime.now();
            this.lastModifiedAt = LocalDateTime.now();
            recalculateTotalPrice();
        }
    }

    public void updateItemQuantity(String productId, Integer quantity) {
        if (this.items != null) {
            this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    this.updatedAt = LocalDateTime.now();
                    this.lastModifiedAt = LocalDateTime.now();
                    recalculateTotalPrice();
                });
        }
    }

    public void clearCart() {
        if (this.items != null) {
            this.items.clear();
        }
        this.totalPrice = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public Integer getItemCount() {
        return (this.items != null) ? this.items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum() : 0;
    }

    private void recalculateTotalPrice() {
        if (this.items == null || this.items.isEmpty()) {
            this.totalPrice = BigDecimal.ZERO;
        } else {
            this.totalPrice = this.items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", itemCount=" + getItemCount() +
                ", totalPrice=" + totalPrice +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
