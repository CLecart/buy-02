package com.example.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItem represents a single product line item within a shopping cart.
 * Embedded document within ShoppingCart.
 */
public class CartItem {

    private String productId;

    private String sellerId;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;

    private String mediaId;

    private LocalDateTime createdAt;

    // Constructors
    public CartItem() {
    }

    public CartItem(String productId, String sellerId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = price.multiply(new BigDecimal(quantity));
        this.createdAt = LocalDateTime.now();
    }

    public CartItem(String productId, String sellerId, String productName, Integer quantity, BigDecimal price, BigDecimal subtotal, LocalDateTime createdAt) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private void recalculateSubtotal() {
        if (this.price != null && this.quantity != null) {
            this.subtotal = this.price.multiply(new BigDecimal(this.quantity));
        }
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
