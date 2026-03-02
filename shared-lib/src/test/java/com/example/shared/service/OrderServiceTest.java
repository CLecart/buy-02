package com.example.shared.service;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.dto.UpdateOrderStatusRequest;
import com.example.shared.event.OrderCreatedEvent;
import com.example.shared.event.OrderStatusChangedEvent;
import com.example.shared.exception.UnauthorizedException;
import com.example.shared.kafka.EventProducer;
import com.example.shared.model.Order;
import com.example.shared.model.OrderItem;
import com.example.shared.model.OrderStatus;
import com.example.shared.model.PaymentMethod;
import com.example.shared.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private SellerProfileService sellerProfileService;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_savesOrder_updatesProfiles_andPublishesEvent() {
        CreateOrderRequest request = createOrderRequest();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(eventProducer).publishOrderCreated(any(OrderCreatedEvent.class));

        OrderDTO dto = orderService.createOrder(request);

        assertThat(dto.buyerId()).isEqualTo("buyer-1");
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.totalPrice()).isEqualByComparingTo("25.00");
        assertThat(dto.status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
        verify(userProfileService).recordNewOrder(eq("buyer-1"), eq(new BigDecimal("25.00")), org.mockito.ArgumentMatchers.<List<OrderItem>>any());
        verify(sellerProfileService, times(2)).recordSale(any(String.class), any(Integer.class), any(BigDecimal.class), any(String.class));
        verify(eventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void getOrderById_throwsWhenMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById("missing"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void updateOrderStatus_updatesTrackingAndPublishesEvent() {
        Order existing = baseOrder("ord-1", "buyer-1", OrderStatus.PENDING);
        when(orderRepository.findById("ord-1")).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(eventProducer).publishOrderStatusChanged(any(OrderStatusChangedEvent.class));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.SHIPPED, "TRACK-1", "Packed and shipped");
        OrderDTO dto = orderService.updateOrderStatus("ord-1", request);

        assertThat(dto.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(dto.trackingNumber()).isEqualTo("TRACK-1");
        verify(orderRepository).save(existing);
        verify(eventProducer).publishOrderStatusChanged(any(OrderStatusChangedEvent.class));
    }

    @Test
    void cancelOrder_throwsUnauthorized_whenRequesterIsNotBuyer() {
        Order existing = baseOrder("ord-2", "buyer-owner", OrderStatus.PENDING);
        when(orderRepository.findById("ord-2")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.cancelOrder("ord-2", "another-user"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to cancel this order");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_throwsWhenDeliveredOrCancelled() {
        Order delivered = baseOrder("ord-3", "buyer-1", OrderStatus.DELIVERED);
        when(orderRepository.findById("ord-3")).thenReturn(Optional.of(delivered));

        assertThatThrownBy(() -> orderService.cancelOrder("ord-3", "buyer-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel order");
    }

    @Test
    void cancelOrder_setsCancelledAndSaves() {
        Order existing = baseOrder("ord-4", "buyer-1", OrderStatus.PENDING);
        when(orderRepository.findById("ord-4")).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO dto = orderService.cancelOrder("ord-4", "buyer-1");

        assertThat(dto.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(existing);
    }

    @Test
    void deleteOrder_throwsUnauthorized_whenBuyerMismatch() {
        Order existing = baseOrder("ord-5", "buyer-1", OrderStatus.PENDING);
        when(orderRepository.findById("ord-5")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.deleteOrder("ord-5", "other"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to remove this order");
    }

    @Test
    void deleteOrder_deletes_whenBuyerMatches() {
        Order existing = baseOrder("ord-6", "buyer-1", OrderStatus.PENDING);
        when(orderRepository.findById("ord-6")).thenReturn(Optional.of(existing));

        orderService.deleteOrder("ord-6", "buyer-1");

        verify(orderRepository).deleteById("ord-6");
    }

    @Test
    void redoOrder_recreatesOrderFromOriginal() {
        Order original = baseOrder("ord-7", "buyer-7", OrderStatus.DELIVERED);
        when(orderRepository.findById("ord-7")).thenReturn(Optional.of(original));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO dto = orderService.redoOrder("ord-7", "buyer-7");

        assertThat(dto.buyerId()).isEqualTo("buyer-7");
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
        verify(eventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void countDelegates_returnRepositoryCounts() {
        when(orderRepository.countByBuyerId("buyer-9")).thenReturn(3L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);

        assertThat(orderService.countOrdersByBuyer("buyer-9")).isEqualTo(3L);
        assertThat(orderService.countOrdersByStatus(OrderStatus.PENDING)).isEqualTo(5L);
    }

    @Test
    void repositoryBasedQueries_mapToDtoPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Order order = baseOrder("ord-9", "buyer-9", OrderStatus.PENDING);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(page);
        when(orderRepository.findByBuyerIdAndStatus("buyer-9", OrderStatus.PENDING, pageable)).thenReturn(page);

        Page<OrderDTO> byStatus = orderService.getOrdersByStatus(OrderStatus.PENDING, pageable);
        Page<OrderDTO> byBuyerStatus = orderService.getOrdersByBuyerAndStatus("buyer-9", OrderStatus.PENDING, pageable);

        assertThat(byStatus.getContent()).hasSize(1);
        assertThat(byBuyerStatus.getContent()).hasSize(1);
    }

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                "buyer-1",
                "buyer1@example.com",
                List.of(
                        new CreateOrderRequest.OrderItemRequest("p-1", "s-1", "Product 1", 2, new BigDecimal("5.00")),
                        new CreateOrderRequest.OrderItemRequest("p-2", "s-2", "Product 2", 1, new BigDecimal("15.00"))
                ),
                PaymentMethod.PAY_ON_DELIVERY,
                "ref-1",
                "Address 1"
        );
    }

    private Order baseOrder(String id, String buyerId, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setBuyerEmail(buyerId + "@example.com");
        order.setItems(List.of(
                new OrderItem("p-1", "s-1", "Product 1", 1, new BigDecimal("10.00")),
                new OrderItem("p-2", "s-2", "Product 2", 1, new BigDecimal("15.00"))
        ));
        order.setTotalPrice(new BigDecimal("25.00"));
        order.setStatus(status);
        order.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);
        order.setPaymentReference("ref");
        order.setShippingAddress("Address");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
