package com.example.shared.kafka.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Base class for all product-related Kafka events.
 * Provides common fields for event tracking and identification.
 */
public abstract class ProductEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String productId;
    private String sellerId;
    private Instant timestamp;
    private EventType eventType;

    /**
     * Types of product events that can be published.
     */
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    protected ProductEvent() {
        this.timestamp = Instant.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    protected ProductEvent(String productId, String sellerId, EventType eventType) {
        this();
        this.productId = productId;
        this.sellerId = sellerId;
        this.eventType = eventType;
    }

    // Getters and Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "ProductEvent{"
                + "eventId='" + eventId + '\''
                + ", productId='" + productId + '\''
                + ", sellerId='" + sellerId + '\''
                + ", timestamp=" + timestamp
                + ", eventType=" + eventType
                + '}';
    }
}
