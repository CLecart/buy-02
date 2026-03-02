package com.example.orderservice.controller;

import com.example.shared.dto.AddToCartRequest;
import com.example.shared.dto.ShoppingCartDTO;
import com.example.shared.dto.UpdateCartItemRequest;
import com.example.shared.exception.UnauthorizedException;
import com.example.shared.service.ShoppingCartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SuppressWarnings("null")
class ShoppingCartControllerTest {

    @Mock
    private ShoppingCartService cartService;

    private ShoppingCartController controller;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new ShoppingCartController(cartService);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void getMyCart_returnsCurrentUserCart() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-1", null));
        ShoppingCartDTO dto = sampleCart("user-1");

        Mockito.when(cartService.getOrCreateCart("user-1")).thenReturn(dto);

        var response = controller.getMyCart();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void addToMyCart_returnsCreatedStatus() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-2", null));
        AddToCartRequest request = new AddToCartRequest("p-1", "s-1", "Product 1", 2, new BigDecimal("12.50"));
        ShoppingCartDTO dto = sampleCart("user-2");

        Mockito.when(cartService.addToCart("user-2", request)).thenReturn(dto);

        var response = controller.addToMyCart(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void updateMyItemQuantity_returnsUpdatedCart() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-3", null));
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        ShoppingCartDTO dto = sampleCart("user-3");

        Mockito.when(cartService.updateItemQuantity("user-3", "p-1", request)).thenReturn(dto);

        var response = controller.updateMyItemQuantity("p-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void deleteMyCart_returnsNoContent() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-4", null));

        var response = controller.deleteMyCart();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(cartService).deleteCart("user-4");
    }

    @Test
    void getCart_throwsUnauthorizedWhenUserMismatch() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("owner", null));

        assertThatThrownBy(() -> controller.getCart("another-user"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to access another user's cart");
    }

    @Test
    void removeFromMyCart_removesItemSuccess() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-5", null));
        ShoppingCartDTO dto = sampleCart("user-5");

        Mockito.when(cartService.removeFromCart("user-5", "p-1")).thenReturn(dto);

        var response = controller.removeFromMyCart("p-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(cartService).removeFromCart("user-5", "p-1");
    }

    @Test
    void clearMyCart_clearsAllItems() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-6", null));
        ShoppingCartDTO empty = new ShoppingCartDTO("cart-1", "user-6", List.of(), 
                BigDecimal.ZERO, 0, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(cartService.clearCart("user-6")).thenReturn(empty);

        var response = controller.clearMyCart();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().items()).isEmpty();
        verify(cartService).clearCart("user-6");
    }

    @Test
    void getCart_allowsBuyerAccess() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-7", null));
        ShoppingCartDTO dto = sampleCart("user-7");

        Mockito.when(cartService.getOrCreateCart("user-7")).thenReturn(dto);

        var response = controller.getCart("user-7");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    private ShoppingCartDTO sampleCart(String userId) {
        var item = new ShoppingCartDTO.CartItemDTO(
                "p-1",
                "s-1",
                "Product 1",
                2,
                new BigDecimal("12.50"),
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );
        return new ShoppingCartDTO(
                "cart-1",
                userId,
                List.of(item),
                new BigDecimal("25.00"),
                2,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
