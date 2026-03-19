package com.example.orderservice.controller;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.dto.UpdateOrderStatusRequest;
import com.example.shared.exception.UnauthorizedException;
import com.example.shared.model.OrderStatus;
import com.example.shared.model.PaymentMethod;
import com.example.shared.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for OrderController covering order management endpoints.
 */
@SuppressWarnings("null")
class OrderControllerSimpleTest {

    @Mock
    private OrderService orderService;

    private OrderController controller;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new OrderController(orderService);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void createOrder_withValidBuyerIdCallsService() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        var item = new CreateOrderRequest.OrderItemRequest("prod-1", "seller-1", 
                "Product 1", 2, new BigDecimal("10.00"));
        var request = new CreateOrderRequest("buyer-1", "buyer@test.com", 
                List.of(item), PaymentMethod.CREDIT_CARD, "ref-123", "Address 1");

        OrderDTO created = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.createOrder(Mockito.any(CreateOrderRequest.class)))
                .thenReturn(created);

        var response = controller.createOrder(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isEqualTo("order-1");
        verify(orderService).createOrder(Mockito.any(CreateOrderRequest.class));
    }

    @Test
    void createOrder_throwsWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        var request = new CreateOrderRequest("buyer-1", "buyer@test.com", 
                List.of(), PaymentMethod.CREDIT_CARD, "", "Address");

        assertThatThrownBy(() -> controller.createOrder(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    void getOrder_allowsBuyerAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        var response = controller.getOrder("order-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo("order-1");
    }

    @Test
    void getOrder_allowsSellerAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-1", null, 
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        var response = controller.getOrder("order-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrder_denyNonBuyerNonSellerAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("other-user", null));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        assertThatThrownBy(() -> controller.getOrder("order-1"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to access this order");
    }

    @Test
    void getMyOrders_returnsBuyerOrders() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByBuyer("buyer-1", PageRequest.of(0, 10), null, null))
                .thenReturn(page);

        var response = controller.getMyOrders(null, null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

        @Test
        void getMyOrders_throwsWhenNotAuthenticated() {
                SecurityContextHolder.clearContext();

                assertThatThrownBy(() -> controller.getMyOrders(null, null, PageRequest.of(0, 10)))
                                .isInstanceOf(UnauthorizedException.class)
                                .hasMessageContaining("Authentication required");
        }

    @Test
    void getMySellerOrders_returnsSellerOrders() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-1", null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersBySeller("seller-1", PageRequest.of(0, 10), null, null))
                .thenReturn(page);

        var response = controller.getMySellerOrders(null, null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

        @Test
        void getMySellerOrders_requiresSellerRole() {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("buyer-1", null));
                var pageable = PageRequest.of(0, 10);

                assertThatThrownBy(() -> controller.getMySellerOrders(null, null, pageable))
                                .isInstanceOf(UnauthorizedException.class)
                                .hasMessageContaining("Seller role required");
        }

    @Test
    void getOrdersByBuyer_enforcesUserMatch() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> controller.getOrdersByBuyer("buyer-2", null, null, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to access other users' orders");
    }

    @Test
    void getOrdersByBuyer_allowsSameUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByBuyer("buyer-1", PageRequest.of(0, 10), null, null))
                .thenReturn(page);

        var response = controller.getOrdersByBuyer("buyer-1", null, null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrdersBySeller_enforcesUserMatch() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-1", null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> controller.getOrdersBySeller("seller-2", null, null, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to access other users' orders");
    }

    @Test
    void getOrdersBySeller_requiresSellerRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> controller.getOrdersBySeller("buyer-1", null, null, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Seller role required");
    }

    @Test
    void getOrdersByBuyer_withSearchParameter() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByBuyer("buyer-1", PageRequest.of(0, 10), "search", null))
                .thenReturn(page);

        var response = controller.getOrdersByBuyer("buyer-1", "search", null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).getOrdersByBuyer("buyer-1", PageRequest.of(0, 10), "search", null);
    }

    @Test
    void getOrdersByBuyer_withStatusFilter() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByBuyer("buyer-1", PageRequest.of(0, 10), null, OrderStatus.PENDING))
                .thenReturn(page);

        var response = controller.getOrdersByBuyer("buyer-1", null, OrderStatus.PENDING, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrdersByStatus_returnsFilteredOrders() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByStatus(OrderStatus.PENDING, PageRequest.of(0, 10)))
                .thenReturn(page);

        var response = controller.getOrdersByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getOrdersByBuyerAndStatus_enforcesUserMatch() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> controller.getOrdersByBuyerAndStatus("buyer-2", OrderStatus.PENDING,
                pageable))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getOrdersByBuyerAndStatus_returnsCombined() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), 
                PageRequest.of(0, 10), 1);
        Mockito.when(orderService.getOrdersByBuyerAndStatus("buyer-1", OrderStatus.PENDING, PageRequest.of(0, 10)))
                .thenReturn(page);

        var response = controller.getOrdersByBuyerAndStatus("buyer-1", OrderStatus.PENDING, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrdersByBuyerInDateRange_returnsOrders() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDateTime.now().minusDays(7);
        var endDate = LocalDateTime.now();

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), pageable, 1);
        Mockito.when(orderService.getOrdersByBuyerInDateRange("buyer-1", startDate, endDate, pageable))
                .thenReturn(page);

        var response = controller.getOrdersByBuyerInDateRange("buyer-1", startDate, endDate, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).getOrdersByBuyerInDateRange("buyer-1", startDate, endDate, pageable);
    }

    @Test
    void getOrdersByBuyerInDateRange_enforcesUserMatch() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDateTime.now().minusDays(7);
        var endDate = LocalDateTime.now();

        assertThatThrownBy(() -> controller.getOrdersByBuyerInDateRange("buyer-2", startDate, endDate, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to access other users' orders");
    }

    @Test
    void getOrdersBySellerInDateRange_requiresSellerRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDateTime.now().minusDays(7);
        var endDate = LocalDateTime.now();

        assertThatThrownBy(() -> controller.getOrdersBySellerInDateRange("buyer-1", startDate, endDate, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Seller role required");
    }

    @Test
    void getOrdersBySellerInDateRange_returnsOrders() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-1", null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDateTime.now().minusDays(7);
        var endDate = LocalDateTime.now();

        Page<OrderDTO> page = new PageImpl<>(List.of(sampleOrder("order-1", "buyer-1")), pageable, 1);
        Mockito.when(orderService.getOrdersBySellerInDateRange("seller-1", startDate, endDate, pageable))
                .thenReturn(page);

        var response = controller.getOrdersBySellerInDateRange("seller-1", startDate, endDate, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).getOrdersBySellerInDateRange("seller-1", startDate, endDate, pageable);
    }

    @Test
    void updateOrderStatus_requiresSellerRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        var request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED, null, null);

        assertThatThrownBy(() -> controller.updateOrderStatus("order-1", request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to update order status");
    }

    @Test
    void updateOrderStatus_requiresSellerOwnership() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-2", null, 
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        var request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED, null, null);

        assertThatThrownBy(() -> controller.updateOrderStatus("order-1", request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to update this order");
    }

    @Test
    void updateOrderStatus_allowsSellerOwner() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller-1", null, 
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        OrderDTO order = sampleOrder("order-1", "buyer-1");
        Mockito.when(orderService.getOrderById("order-1")).thenReturn(order);

        OrderDTO updated = new OrderDTO("order-1", "buyer-1", "buyer@test.com", 
                order.items(), order.totalPrice(), OrderStatus.CONFIRMED, order.paymentMethod(),
                "ref-123", "Address 1", "track-123", order.createdAt(), LocalDateTime.now());
        Mockito.when(orderService.updateOrderStatus(Mockito.eq("order-1"), Mockito.any(UpdateOrderStatusRequest.class)))
                .thenReturn(updated);

        var response = controller.updateOrderStatus("order-1", 
                new UpdateOrderStatusRequest(OrderStatus.CONFIRMED, "track-123", null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cancelOrder_callsService() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        OrderDTO cancelled = new OrderDTO("order-1", "buyer-1", "buyer@test.com", 
                List.of(), BigDecimal.ZERO, OrderStatus.CANCELLED, PaymentMethod.CREDIT_CARD,
                "ref-123", "Address 1", null, LocalDateTime.now(), LocalDateTime.now());
        Mockito.when(orderService.cancelOrder("order-1", "buyer-1")).thenReturn(cancelled);

        var response = controller.cancelOrder("order-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderService).cancelOrder("order-1", "buyer-1");
    }

        @Test
        void cancelOrder_throwsWhenNotAuthenticated() {
                SecurityContextHolder.clearContext();

                assertThatThrownBy(() -> controller.cancelOrder("order-1"))
                                .isInstanceOf(UnauthorizedException.class)
                                .hasMessageContaining("Authentication required");
        }

    @Test
    void deleteOrder_callsService() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        doNothing().when(orderService).deleteOrder("order-1", "buyer-1");

        var response = controller.deleteOrder("order-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderService).deleteOrder("order-1", "buyer-1");
    }

        @Test
        void deleteOrder_throwsWhenNotAuthenticated() {
                SecurityContextHolder.clearContext();

                assertThatThrownBy(() -> controller.deleteOrder("order-1"))
                                .isInstanceOf(UnauthorizedException.class)
                                .hasMessageContaining("Authentication required");
        }

    @Test
    void redoOrder_callsService() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        OrderDTO redone = sampleOrder("order-new", "buyer-1");
        Mockito.when(orderService.redoOrder("order-1", "buyer-1")).thenReturn(redone);

        var response = controller.redoOrder("order-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isEqualTo("order-new");
    }

        @Test
        void redoOrder_throwsWhenNotAuthenticated() {
                SecurityContextHolder.clearContext();

                assertThatThrownBy(() -> controller.redoOrder("order-1"))
                                .isInstanceOf(UnauthorizedException.class)
                                .hasMessageContaining("Authentication required");
        }

    @Test
    void countOrdersByBuyer_enforcesUserMatch() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        assertThatThrownBy(() -> controller.countOrdersByBuyer("buyer-2"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void countOrdersByBuyer_returnsCount() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer-1", null));

        Mockito.when(orderService.countOrdersByBuyer("buyer-1")).thenReturn(5L);

        var response = controller.countOrdersByBuyer("buyer-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    void countOrdersByStatus_returnsCount() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null));

        Mockito.when(orderService.countOrdersByStatus(OrderStatus.PENDING)).thenReturn(10L);

        var response = controller.countOrdersByStatus(OrderStatus.PENDING);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(10L);
    }

    // Helper method to create a sample order
    private OrderDTO sampleOrder(String orderId, String buyerId) {
        var item = new OrderDTO.OrderItemDTO("prod-1", "seller-1", "Product 1", 2, 
                new BigDecimal("10.00"), new BigDecimal("20.00"));
        return new OrderDTO(
                orderId,
                buyerId,
                "buyer@test.com",
                List.of(item),
                new BigDecimal("20.00"),
                OrderStatus.PENDING,
                PaymentMethod.CREDIT_CARD,
                "ref-123",
                "Address 1",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
