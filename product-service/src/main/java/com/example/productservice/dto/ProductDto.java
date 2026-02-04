package com.example.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product data transfer object used by product API endpoints.
 *
 * <p>Note: the {@code ownerId} field, when supplied by a client, is ignored on create and
 * replaced by the authenticated JWT subject. It is returned to clients for informational
 * purposes on reads.
 */
public class ProductDto {
    private String id;

    private String ownerId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    /** Quantity in stock. */
    private Integer quantity;

    /** List of media IDs associated with this product. */
    private List<String> mediaIds;

    public ProductDto() {}

    public ProductDto(String id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public ProductDto(String id, String name, String description, BigDecimal price, String ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
    }

    public ProductDto(String id, String name, String description, BigDecimal price, String ownerId, List<String> mediaIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
        this.mediaIds = mediaIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public List<String> getMediaIds() { return mediaIds; }
    public void setMediaIds(List<String> mediaIds) { this.mediaIds = mediaIds; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
