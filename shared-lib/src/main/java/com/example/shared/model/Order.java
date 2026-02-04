package com.example.shared.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * Order entity representing a purchase order in the e-commerce platform.
 * Stored in MongoDB collection "orders".
 */
@Document(collection = "orders")
@CompoundIndexes({
    @CompoundIndex(name = "buyerId_createdAt_idx", def = "{'buyerId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "sellerId_createdAt_idx", def = "{'items.sellerId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': -1}")
})
public class Order {

    @Id
    private String id;

    @Indexed
    private String buyerId;

    private List<OrderItem> items;

    private BigDecimal totalPrice;

    private BigDecimal shippingCost;

    @Indexed
    private OrderStatus status;

    private PaymentMethod paymentMethod;

    private String shippingAddress;

    private String trackingNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime estimatedDeliveryDate;

    private LocalDateTime deliveredAt;

    private String notes;

    // Constructors
    public Order() {
    }

    public Order(String buyerId, List<OrderItem> items, BigDecimal totalPrice) {
        this.buyerId = buyerId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
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

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
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

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", buyerId='" + buyerId + '\'' +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}
