package com.example.orderservice.controller;

import com.example.shared.dto.AddToCartRequest;
import com.example.shared.dto.ShoppingCartDTO;
import com.example.shared.dto.UpdateCartItemRequest;
import com.example.shared.exception.UnauthorizedException;
import com.example.shared.service.ShoppingCartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Shopping Cart Management.
 * Provides endpoints for add/remove items and cart operations.
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    /**
     * Get shopping cart for a user.
     * GET /api/carts/{userId}
     *
     * @param userId the user's ID
     * @return shopping cart DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCartDTO> getCart(@PathVariable String userId) {
        log.info("GET /api/carts/{} - Fetching cart", userId);
        enforceUserMatch(userId);
        ShoppingCartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Get current user's cart.
     * GET /api/carts/me
     */
    @GetMapping("/me")
    public ResponseEntity<ShoppingCartDTO> getMyCart() {
        String userId = getCurrentUserId();
        ShoppingCartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add item to cart.
     * POST /api/carts/{userId}/items
     *
     * @param userId the user's ID
     * @param request add to cart request
     * @return updated cart
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<ShoppingCartDTO> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request
    ) {
        log.info("POST /api/carts/{}/items - Adding product: {}", userId, request.productId());
        enforceUserMatch(userId);
        ShoppingCartDTO updatedCart = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    /**
     * Add item to current user's cart.
     * POST /api/carts/me/items
     */
    @PostMapping("/me/items")
    public ResponseEntity<ShoppingCartDTO> addToMyCart(@Valid @RequestBody AddToCartRequest request) {
        String userId = getCurrentUserId();
        ShoppingCartDTO updatedCart = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    /**
     * Update item quantity in cart.
     * PATCH /api/carts/{userId}/items/{productId}
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @param request update quantity request
     * @return updated cart
     */
    @PatchMapping("/{userId}/items/{productId}")
    public ResponseEntity<ShoppingCartDTO> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        log.info("PATCH /api/carts/{}/items/{} - Updating quantity to: {}", userId, productId, request.quantity());
        enforceUserMatch(userId);
        ShoppingCartDTO updatedCart = cartService.updateItemQuantity(userId, productId, request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Update item quantity in current user's cart.
     * PATCH /api/carts/me/items/{productId}
     */
    @PatchMapping("/me/items/{productId}")
    public ResponseEntity<ShoppingCartDTO> updateMyItemQuantity(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        String userId = getCurrentUserId();
        ShoppingCartDTO updatedCart = cartService.updateItemQuantity(userId, productId, request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Remove item from cart.
     * DELETE /api/carts/{userId}/items/{productId}
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @return updated cart
     */
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ShoppingCartDTO> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId
    ) {
        log.info("DELETE /api/carts/{}/items/{} - Removing item", userId, productId);
        enforceUserMatch(userId);
        ShoppingCartDTO updatedCart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Remove item from current user's cart.
     * DELETE /api/carts/me/items/{productId}
     */
    @DeleteMapping("/me/items/{productId}")
    public ResponseEntity<ShoppingCartDTO> removeFromMyCart(@PathVariable String productId) {
        String userId = getCurrentUserId();
        ShoppingCartDTO updatedCart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Clear all items from cart.
     * DELETE /api/carts/{userId}
     *
     * @param userId the user's ID
     * @return empty cart
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ShoppingCartDTO> clearCart(@PathVariable String userId) {
        log.info("DELETE /api/carts/{} - Clearing cart", userId);
        enforceUserMatch(userId);
        ShoppingCartDTO clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(clearedCart);
    }

    /**
     * Clear current user's cart.
     * DELETE /api/carts/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<ShoppingCartDTO> clearMyCart() {
        String userId = getCurrentUserId();
        ShoppingCartDTO clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(clearedCart);
    }

    /**
     * Delete cart completely.
     * DELETE /api/carts/{userId}/delete
     *
     * @param userId the user's ID
     * @return no content response
     */
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<Void> deleteCart(@PathVariable String userId) {
        log.info("DELETE /api/carts/{}/delete - Deleting cart", userId);
        enforceUserMatch(userId);
        cartService.deleteCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete current user's cart.
     * DELETE /api/carts/me/delete
     */
    @DeleteMapping("/me/delete")
    public ResponseEntity<Void> deleteMyCart() {
        String userId = getCurrentUserId();
        cartService.deleteCart(userId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "";
        }
        return auth.getName();
    }

    private void enforceUserMatch(String userId) {
        String currentUser = getCurrentUserId();
        if (!userId.equals(currentUser)) {
            throw new UnauthorizedException("Not allowed to access another user's cart");
        }
    }
}
