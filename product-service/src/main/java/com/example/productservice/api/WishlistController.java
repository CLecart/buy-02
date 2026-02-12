package com.example.productservice.api;

import com.example.productservice.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * REST controller for managing user wishlists.
     */
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * Retrieves the wishlist for the authenticated user.
     * @param principal the authenticated user principal
     * @return the user's wishlist
     */
    @GetMapping
    public ResponseEntity<Object> getWishlist(Principal principal) {
        return ResponseEntity.ok(wishlistService.getWishlist(principal.getName()));
    }

    /**
     * Adds a product to the user's wishlist.
     * @param body request body containing productId
     * @param principal the authenticated user principal
     * @return success response
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Boolean>> addToWishlist(@RequestBody Map<String, String> body, Principal principal) {
        wishlistService.addToWishlist(principal.getName(), body.get("productId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Removes a product from the user's wishlist.
     * @param body request body containing productId
     * @param principal the authenticated user principal
     * @return success response
     */
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Boolean>> removeFromWishlist(@RequestBody Map<String, String> body, Principal principal) {
        wishlistService.removeFromWishlist(principal.getName(), body.get("productId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Clears the user's wishlist.
     * @param principal the authenticated user principal
     * @return empty response
     */
    @PostMapping("/clear")
    public ResponseEntity<Void> clearWishlist(Principal principal) {
        wishlistService.clearWishlist(principal.getName());
        return ResponseEntity.ok().build();
    }
}
