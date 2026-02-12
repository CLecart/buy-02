package com.example.productservice.service;

import com.example.productservice.model.Wishlist;
import com.example.productservice.model.WishlistItem;
import com.example.productservice.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service for managing user wishlists.
 */
@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    @Autowired
    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    /**
     * Retrieves the wishlist for a given user.
     * @param userId the user identifier
     * @return the user's wishlist
     */
    public Wishlist getWishlist(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUserId(userId);
                    wishlist.setCreatedAt(new Date());
                    wishlist.setUpdatedAt(new Date());
                    return wishlistRepository.save(wishlist);
                });
    }

    /**
     * Adds a product to the user's wishlist.
     * @param userId the user identifier
     * @param productId the product identifier
     */
    public void addToWishlist(String userId, String productId) {
        Wishlist wishlist = getWishlist(userId);
        boolean exists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        if (!exists) {
            WishlistItem item = new WishlistItem();
            item.setProductId(productId);
            // Optionally set productName, price, etc. via ProductService
            wishlist.getItems().add(item);
            wishlist.setUpdatedAt(new Date());
            wishlistRepository.save(wishlist);
        }
    }

    /**
     * Removes a product from the user's wishlist.
     * @param userId the user identifier
     * @param productId the product identifier
     */
    public void removeFromWishlist(String userId, String productId) {
        Wishlist wishlist = getWishlist(userId);
        wishlist.getItems().removeIf(item -> item.getProductId().equals(productId));
        wishlist.setUpdatedAt(new Date());
        wishlistRepository.save(wishlist);
    }

    /**
     * Clears the user's wishlist.
     * @param userId the user identifier
     */
    public void clearWishlist(String userId) {
        Wishlist wishlist = getWishlist(userId);
        wishlist.getItems().clear();
        wishlist.setUpdatedAt(new Date());
        wishlistRepository.save(wishlist);
    }
}
