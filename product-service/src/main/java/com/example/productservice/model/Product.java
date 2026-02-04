package com.example.productservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product document persisted in the `products` collection.
 *
 * Business note: each Product is owned by a User. The owner's id is stored in
 * {@code ownerId} and MUST reference the {@code User.id} in the user-service.
 * This field is used for ownership checks (modify/delete) and must be validated
 * at the application level (no DB foreign keys in MongoDB).
 */
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
    /** Owner user id — references the owning User (user.id). */
    @Indexed
    private String ownerId;

    /** Quantity in stock. */
    private Integer quantity;

    /** List of media IDs associated with this product. */
    private List<String> mediaIds = new ArrayList<>();

    public Product() {
    }

    public Product(String name, String description, BigDecimal price, String ownerId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
        this.quantity = 0;
        this.mediaIds = new ArrayList<>();
    }

    public Product(String name, String description, BigDecimal price, String ownerId, Integer quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
        this.quantity = quantity;
        this.mediaIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<String> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
    }

    public void addMediaId(String mediaId) {
        if (this.mediaIds == null) {
            this.mediaIds = new ArrayList<>();
        }
        this.mediaIds.add(mediaId);
    }

    public void removeMediaId(String mediaId) {
        if (this.mediaIds != null) {
            this.mediaIds.remove(mediaId);
        }
    }
}
