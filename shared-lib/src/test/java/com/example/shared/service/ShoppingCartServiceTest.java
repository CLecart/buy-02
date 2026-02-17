package com.example.shared.service;

import com.example.shared.dto.AddToCartRequest;
import com.example.shared.dto.ShoppingCartDTO;
import com.example.shared.dto.UpdateCartItemRequest;
import com.example.shared.model.CartItem;
import com.example.shared.model.ShoppingCart;
import com.example.shared.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
/**
 * Unit tests for {@link ShoppingCartService} covering cart operations.
 */
class ShoppingCartServiceTest {

    @Mock
    ShoppingCartRepository cartRepository;

    @InjectMocks
    ShoppingCartService cartService;

    // use AtomicReference to capture saved carts and avoid null/unchecked warnings
    private final java.util.concurrent.atomic.AtomicReference<ShoppingCart> savedRef = new java.util.concurrent.atomic.AtomicReference<>();

    private final String userId = "user-123";

    @BeforeEach
    void setup() {
    }

    @Test
    /**
     * When a user without a cart adds an item, a new cart is created and the
     * item is added with correct totals.
     */
    void addToCart_createsNewCartAndAddsItem() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> {
            ShoppingCart s = i.getArgument(0);
            savedRef.set(s);
            return s;
        });

        AddToCartRequest req = new AddToCartRequest("prod-1", "seller-a", "Product 1", 2, new BigDecimal("10.00"));

        ShoppingCartDTO dto = cartService.addToCart(userId, req);

        // savedRef was set by the save() answer; get the last saved cart
        verify(cartRepository, times(2)).save(any(ShoppingCart.class));
        ShoppingCart saved = Objects.requireNonNull(savedRef.get());

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getItems()).hasSize(1);
        CartItem item = saved.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo("prod-1");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("20.00"));

        assertThat(dto.itemCount()).isEqualTo(saved.getItemCount());
    }

    @Test
    /**
     * Adding an item that already exists in the cart should merge quantities
     * and update the total price accordingly.
     */
    void addToCart_mergesExistingItemQuantities() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(BigDecimal.ZERO);
        existing.setItemCount(0);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        CartItem item = new CartItem("prod-1", "seller-a", "Product 1", 1, new BigDecimal("5.00"));
        existing.getItems().add(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> {
            ShoppingCart s = i.getArgument(0);
            savedRef.set(s);
            return s;
        });

        AddToCartRequest req = new AddToCartRequest("prod-1", "seller-a", "Product 1", 3, new BigDecimal("5.00"));

        ShoppingCartDTO dto = cartService.addToCart(userId, req);

        verify(cartRepository).save(any(ShoppingCart.class));
        ShoppingCart saved = Objects.requireNonNull(savedRef.get());

        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(4);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("20.00"));
        // also assert DTO maps correctly to ensure dto is used and no unused-variable warnings remain
        assertThat(dto.itemCount()).isEqualTo(saved.getItemCount());
    }

    @Test
    /**
     * Updating an item's quantity recalculates totals and item counts.
     */
    void updateItemQuantity_updatesAndRecalculates() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(BigDecimal.ZERO);
        existing.setItemCount(0);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        CartItem item = new CartItem("prod-1", "seller-a", "Product 1", 2, new BigDecimal("3.50"));
        existing.getItems().add(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> {
            ShoppingCart s = i.getArgument(0);
            savedRef.set(s);
            return s;
        });

        UpdateCartItemRequest req = new UpdateCartItemRequest(5);
        ShoppingCartDTO dto = cartService.updateItemQuantity(userId, "prod-1", req);

        verify(cartRepository).save(any(ShoppingCart.class));
        ShoppingCart saved = Objects.requireNonNull(savedRef.get());

        assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("17.50"));
        assertThat(dto.itemCount()).isEqualTo(5);
    }

    @Test
    /**
     * Removing an item should delete it from the cart and adjust totals.
     */
    void removeFromCart_removesItem() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(BigDecimal.ZERO);
        existing.setItemCount(0);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        CartItem item1 = new CartItem("prod-1", "seller-a", "Product 1", 2, new BigDecimal("3.00"));
        CartItem item2 = new CartItem("prod-2", "seller-b", "Product 2", 1, new BigDecimal("7.00"));
        existing.getItems().add(item1);
        existing.getItems().add(item2);


        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> {
            ShoppingCart s = i.getArgument(0);
            savedRef.set(s);
            return s;
        });

        ShoppingCartDTO dto = cartService.removeFromCart(userId, "prod-1");

        verify(cartRepository).save(any(ShoppingCart.class));
        ShoppingCart saved = Objects.requireNonNull(savedRef.get());

        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getProductId()).isEqualTo("prod-2");
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("7.00"));
        // ensure dto is used to avoid unused-variable warnings
        assertThat(dto.itemCount()).isEqualTo(saved.getItemCount());
    }
}
