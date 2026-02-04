package com.example.shared.model;

/**
 * OrderStatus enum representing the various states of an order.
 */
public enum OrderStatus {
    PENDING("Pending - Payment not processed"),
    CONFIRMED("Confirmed - Payment processed, awaiting shipment"),
    SHIPPED("Shipped - Package in transit"),
    DELIVERED("Delivered - Successfully delivered to customer"),
    CANCELLED("Cancelled - Order cancelled by buyer or system"),
    RETURNED("Returned - Product returned by buyer"),
    REFUNDED("Refunded - Money refunded to buyer");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
