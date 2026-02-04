package com.example.shared.kafka.event;

import java.math.BigDecimal;

/**
 * Event published when a product is updated.
 * Contains updated product details for consumers to react to product changes.
 */
public class ProductUpdatedEvent extends ProductEvent {

    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;

    public ProductUpdatedEvent() {
        super();
        setEventType(EventType.UPDATED);
    }

    public ProductUpdatedEvent(String productId, String sellerId, String name,
                               String description, BigDecimal price, Integer quantity) {
        super(productId, sellerId, EventType.UPDATED);
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductUpdatedEvent{"
                + "productId='" + getProductId() + '\''
                + ", sellerId='" + getSellerId() + '\''
                + ", name='" + name + '\''
                + ", price=" + price
                + ", quantity=" + quantity
                + ", timestamp=" + getTimestamp()
                + '}';
    }
}
