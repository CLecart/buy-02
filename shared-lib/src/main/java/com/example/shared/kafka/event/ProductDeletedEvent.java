package com.example.shared.kafka.event;

/**
 * Event published when a product is deleted.
 * Media-service consumes this to cleanup orphan media files.
 */
public class ProductDeletedEvent extends ProductEvent {

    private static final long serialVersionUID = 1L;

    public ProductDeletedEvent() {
        super();
        setEventType(EventType.DELETED);
    }

    public ProductDeletedEvent(String productId, String sellerId) {
        super(productId, sellerId, EventType.DELETED);
    }

    @Override
    public String toString() {
        return "ProductDeletedEvent{"
                + "productId='" + getProductId() + '\''
                + ", sellerId='" + getSellerId() + '\''
                + ", timestamp=" + getTimestamp()
                + '}';
    }
}
