package com.example.productservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a user's wishlist in the e-commerce platform.
 */
@Document(collection = "wishlists")
public class Wishlist {
    /**
     * The unique identifier of the wishlist.
     */
    @Id
    private String id;

    /**
     * The unique identifier of the user (one wishlist per user).
     */
    @Indexed(unique = true)
    private String userId;

    /**
     * The list of items in the wishlist.
     */
    private List<WishlistItem> items = new ArrayList<>();

    /**
     * The creation date of the wishlist.
     */
    private Date createdAt;

    /**
     * The last update date of the wishlist.
     */
    private Date updatedAt;



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

    public List<WishlistItem> getItems() {
        return items;
    }

    public void setItems(List<WishlistItem> items) {
        this.items = items;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
