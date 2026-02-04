package com.example.shared.service;

import com.example.shared.dto.AddToCartRequest;
import com.example.shared.dto.ShoppingCartDTO;
import com.example.shared.dto.UpdateCartItemRequest;
import com.example.shared.model.CartItem;
import com.example.shared.model.ShoppingCart;
import com.example.shared.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Shopping Cart management.
 * Handles add/remove items, quantity updates, and cart operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartService {

    private static final String CART_NOT_FOUND_MSG = "Cart not found for user: ";
    private static final String ITEM_NOT_FOUND_MSG = "Item not found in cart: ";

    private final ShoppingCartRepository cartRepository;

    /**
     * Get or create shopping cart for a user.
     *
     * @param userId the user's ID
     * @return the shopping cart DTO
     */
    public ShoppingCartDTO getOrCreateCart(String userId) {
        log.debug("Getting or creating cart for user: {}", userId);

        Optional<ShoppingCart> existingCart = cartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            return mapToDTO(existingCart.get());
        }

        // Create new cart
        ShoppingCart newCart = new ShoppingCart();
        newCart.setId(UUID.randomUUID().toString());
        newCart.setUserId(userId);
        newCart.setItems(java.util.Collections.emptyList());
        newCart.setTotalPrice(BigDecimal.ZERO);
        newCart.setItemCount(0);
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setUpdatedAt(LocalDateTime.now());

        ShoppingCart savedCart = cartRepository.save(newCart);
        log.info("New cart created for user: {}", userId);
        return mapToDTO(savedCart);
    }

    /**
     * Get cart by user ID.
     *
     * @param userId the user's ID
     * @return the shopping cart DTO
     * @throws NoSuchElementException if cart not found
     */
    public ShoppingCartDTO getCart(String userId) {
        log.debug("Fetching cart for user: {}", userId);
        return cartRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new NoSuchElementException(CART_NOT_FOUND_MSG + userId));
    }

    /**
     * Add item to cart (or merge if product already exists).
     *
     * @param userId the user's ID
     * @param request add to cart request
     * @return updated cart DTO
     */
    @Transactional
    public ShoppingCartDTO addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item to cart - User: {}, Product: {}", userId, request.productId());

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // Check if item already exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId())
                        && item.getSellerId().equals(request.sellerId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Merge quantities
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
            item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            log.debug("Merged quantity for existing item: {}", request.productId());
        } else {
            // Add new item
            CartItem newItem = new CartItem(
                    request.productId(),
                    request.sellerId(),
                    request.productName(),
                    request.quantity(),
                    request.price(),
                    request.price().multiply(BigDecimal.valueOf(request.quantity())),
                    LocalDateTime.now()
            );
            cart.getItems().add(newItem);
            log.debug("New item added to cart: {}", request.productId());
        }

        recalculateCart(cart);
        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("Item added successfully to cart");
        return mapToDTO(savedCart);
    }

    /**
     * Update item quantity in cart.
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @param request update request with new quantity
     * @return updated cart DTO
     */
    @Transactional
    public ShoppingCartDTO updateItemQuantity(String userId, String productId, UpdateCartItemRequest request) {
        log.info("Updating item quantity - User: {}, Product: {}, Quantity: {}", 
                userId, productId, request.quantity());

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(CART_NOT_FOUND_MSG + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(ITEM_NOT_FOUND_MSG + productId));

        item.setQuantity(request.quantity());
        item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(request.quantity())));

        recalculateCart(cart);
        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("Item quantity updated successfully");
        return mapToDTO(savedCart);
    }

    /**
     * Remove item from cart.
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @return updated cart DTO
     */
    @Transactional
    public ShoppingCartDTO removeFromCart(String userId, String productId) {
        log.info("Removing item from cart - User: {}, Product: {}", userId, productId);

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(CART_NOT_FOUND_MSG + userId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new NoSuchElementException(ITEM_NOT_FOUND_MSG + productId);
        }

        recalculateCart(cart);
        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("Item removed successfully from cart");
        return mapToDTO(savedCart);
    }

    /**
     * Clear all items from cart.
     *
     * @param userId the user's ID
     * @return empty cart DTO
     */
    @Transactional
    public ShoppingCartDTO clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(CART_NOT_FOUND_MSG + userId));

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setItemCount(0);
        cart.setUpdatedAt(LocalDateTime.now());

        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("Cart cleared successfully");
        return mapToDTO(savedCart);
    }

    /**
     * Delete cart completely.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void deleteCart(String userId) {
        log.info("Deleting cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
    }

    /**
     * Create a new shopping cart for a user.
     */
    private ShoppingCart createNewCart(String userId) {
        ShoppingCart newCart = new ShoppingCart();
        newCart.setId(UUID.randomUUID().toString());
        newCart.setUserId(userId);
        newCart.setItems(java.util.Collections.emptyList());
        newCart.setTotalPrice(BigDecimal.ZERO);
        newCart.setItemCount(0);
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(newCart);
    }

    /**
     * Recalculate cart totals.
     */
    private void recalculateCart(ShoppingCart cart) {
        BigDecimal totalPrice = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalPrice(totalPrice);
        cart.setItemCount(itemCount);
        cart.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Map ShoppingCart entity to ShoppingCartDTO.
     */
    private ShoppingCartDTO mapToDTO(ShoppingCart cart) {
        var itemDTOs = cart.getItems().stream()
                .map(item -> new ShoppingCartDTO.CartItemDTO(
                        item.getProductId(),
                        item.getSellerId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal(),
                        item.getCreatedAt()
                ))
                .toList();

        return new ShoppingCartDTO(
                cart.getId(),
                cart.getUserId(),
                itemDTOs,
                cart.getTotalPrice(),
                cart.getItemCount(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
