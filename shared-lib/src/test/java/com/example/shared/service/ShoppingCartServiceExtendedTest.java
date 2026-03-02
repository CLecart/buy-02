package com.example.shared.service;

import com.example.shared.dto.ShoppingCartDTO;
import com.example.shared.model.CartItem;
import com.example.shared.model.ShoppingCart;
import com.example.shared.repository.ShoppingCartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Extended unit tests for {@link ShoppingCartService} covering additional operations
 * like getCart, clearCart, deleteCart, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ShoppingCartServiceExtendedTest {

    @Mock
    ShoppingCartRepository cartRepository;

    @InjectMocks
    ShoppingCartService cartService;

    private final String userId = "user-456";

    @Test
    void getOrCreateCart_returnsExistingCart() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(new BigDecimal("25.00"));
        existing.setItemCount(3);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        ShoppingCartDTO dto = cartService.getOrCreateCart(userId);

        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(dto.itemCount()).isEqualTo(3);
        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void getOrCreateCart_createsNewCartWhenNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartDTO dto = cartService.getOrCreateCart(userId);

        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.itemCount()).isZero();
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void getCart_returnsCartWhenExists() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(new BigDecimal("15.00"));
        existing.setItemCount(2);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        ShoppingCartDTO dto = cartService.getCart(userId);

        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void getCart_throwsWhenCartNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(userId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Cart not found for user: " + userId);
    }

    @Test
    void clearCart_removesAllItemsAndResetsTotal() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());

        CartItem item1 = new CartItem("prod-1", "seller-a", "Product 1", 2, new BigDecimal("5.00"));
        CartItem item2 = new CartItem("prod-2", "seller-b", "Product 2", 3, new BigDecimal("8.00"));
        existing.getItems().add(item1);
        existing.getItems().add(item2);
        existing.setTotalPrice(new BigDecimal("34.00"));
        existing.setItemCount(5);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartDTO dto = cartService.clearCart(userId);

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.itemCount()).isZero();
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void clearCart_throwsWhenCartNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart(userId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Cart not found for user: " + userId);
    }

    @Test
    void deleteCart_callsRepositoryDelete() {
        doNothing().when(cartRepository).deleteByUserId(userId);

        cartService.deleteCart(userId);

        verify(cartRepository).deleteByUserId(userId);
    }

    @Test
    void updateItemQuantity_throwsWhenCartNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        var request = new com.example.shared.dto.UpdateCartItemRequest(5);

        assertThatThrownBy(() -> cartService.updateItemQuantity(userId, "prod-1", request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Cart not found for user: " + userId);
    }

    @Test
    void updateItemQuantity_throwsWhenItemNotFound() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(BigDecimal.ZERO);
        existing.setItemCount(0);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        var request = new com.example.shared.dto.UpdateCartItemRequest(10);

        assertThatThrownBy(() -> cartService.updateItemQuantity(userId, "prod-nonexistent", request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Item not found in cart: prod-nonexistent");
    }

    @Test
    void removeFromCart_throwsWhenCartNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeFromCart(userId, "prod-1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Cart not found for user: " + userId);
    }

    @Test
    void removeFromCart_throwsWhenItemNotFound() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(UUID.randomUUID().toString());
        existing.setUserId(userId);
        existing.setItems(new ArrayList<>());
        existing.setTotalPrice(BigDecimal.ZERO);
        existing.setItemCount(0);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> cartService.removeFromCart(userId, "prod-missing"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Item not found in cart: prod-missing");
    }
}
