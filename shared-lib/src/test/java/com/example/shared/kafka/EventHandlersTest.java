package com.example.shared.kafka;

import com.example.shared.event.OrderCreatedEvent;
import com.example.shared.exception.EventProcessingException;
import com.example.shared.service.SellerProfileService;
import com.example.shared.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Kafka event handlers.
 */
@ExtendWith(MockitoExtension.class)
class EventHandlersTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private SellerProfileService sellerProfileService;

    @InjectMocks
    private OrderCreatedEventHandler orderCreatedEventHandler;

    private OrderCreatedEvent testEvent;

    @BeforeEach
    void setUp() {
        var item1 = new OrderCreatedEvent.OrderItemSnapshot("prod-1", "seller-a", "Product 1", 
                2, new BigDecimal("10.00"), new BigDecimal("20.00"));
        var item2 = new OrderCreatedEvent.OrderItemSnapshot("prod-2", "seller-b", "Product 2", 
                1, new BigDecimal("15.00"), new BigDecimal("15.00"));

        testEvent = new OrderCreatedEvent("order-123", "buyer-1", "buyer@test.com",
                List.of(item1, item2), new BigDecimal("35.00"), "Address 1", 
                java.time.LocalDateTime.now());
    }

    @Test
    void handleOrderCreated_updatesProfilesSuccessfully() {
        doNothing().when(userProfileService).recordNewOrder(eq("buyer-1"), 
                eq(new BigDecimal("35.00")), any());
        doNothing().when(sellerProfileService).recordSale(anyString(), anyInt(), 
                any(BigDecimal.class), anyString());

        orderCreatedEventHandler.handleOrderCreated(testEvent);

        verify(userProfileService).recordNewOrder(eq("buyer-1"), eq(new BigDecimal("35.00")), any());
        verify(sellerProfileService).recordSale("seller-a", 2, new BigDecimal("20.00"), "prod-1");
        verify(sellerProfileService).recordSale("seller-b", 1, new BigDecimal("15.00"), "prod-2");
    }

    @Test
    void handleOrderCreated_throwsEventProcessingExceptionOnError() {
        doThrow(new RuntimeException("Database error"))
                .when(userProfileService).recordNewOrder(anyString(), any(BigDecimal.class), any());

        assertThatThrownBy(() -> orderCreatedEventHandler.handleOrderCreated(testEvent))
                .isInstanceOf(EventProcessingException.class)
                .hasMessageContaining("Failed to process order created event");

        verify(userProfileService).recordNewOrder(eq("buyer-1"), eq(new BigDecimal("35.00")), any());
    }

    @Test
    void handleOrderCreated_processesEmptyItemsList() {
        var emptyEvent = new OrderCreatedEvent("order-empty", "buyer-1", "buyer@test.com",
                List.of(), BigDecimal.ZERO, "Address 1", java.time.LocalDateTime.now());

        doNothing().when(userProfileService).recordNewOrder(anyString(), any(BigDecimal.class), any());

        orderCreatedEventHandler.handleOrderCreated(emptyEvent);

        verify(userProfileService).recordNewOrder(eq("buyer-1"), eq(BigDecimal.ZERO), any());
        verify(sellerProfileService, never()).recordSale(anyString(), anyInt(), any(BigDecimal.class), 
                anyString());
    }

    @Test
    void handleOrderStatusChanged_processesShippedStatus() {
        OrderStatusChangedEventHandler handler = new OrderStatusChangedEventHandler();
        var event = new com.example.shared.event.OrderStatusChangedEvent("order-1", "buyer-1",
                "buyer@test.com", com.example.shared.model.OrderStatus.CONFIRMED,
                com.example.shared.model.OrderStatus.SHIPPED, null, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleOrderStatusChanged(event)).doesNotThrowAnyException();
    }

    @Test
    void handleOrderStatusChanged_processesDeliveredStatus() {
        OrderStatusChangedEventHandler handler = new OrderStatusChangedEventHandler();
        var event = new com.example.shared.event.OrderStatusChangedEvent("order-2", "buyer-2", "buyer2@test.com",
                com.example.shared.model.OrderStatus.SHIPPED, com.example.shared.model.OrderStatus.DELIVERED,
                null, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleOrderStatusChanged(event)).doesNotThrowAnyException();
    }

    @Test
    void handleOrderStatusChanged_processesCancelledStatus() {
        OrderStatusChangedEventHandler handler = new OrderStatusChangedEventHandler();
        var event = new com.example.shared.event.OrderStatusChangedEvent("order-3", "buyer-3", "buyer3@test.com",
                com.example.shared.model.OrderStatus.PENDING, com.example.shared.model.OrderStatus.CANCELLED,
                "Customer requested", java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleOrderStatusChanged(event)).doesNotThrowAnyException();
    }

    @Test
    void handleOrderStatusChanged_handlesOtherStatuses() {
        OrderStatusChangedEventHandler handler = new OrderStatusChangedEventHandler();
        var event = new com.example.shared.event.OrderStatusChangedEvent("order-4", "buyer-4", "buyer4@test.com",
                com.example.shared.model.OrderStatus.PENDING, com.example.shared.model.OrderStatus.CONFIRMED,
                null, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleOrderStatusChanged(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_processesItemAddedAction() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent("cart-1", "user-1", "ITEM_ADDED",
                "prod-1", 3, new BigDecimal("10.00"), 3, new BigDecimal("30.00"), java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_processesItemRemovedAction() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent("cart-1", "user-1", "ITEM_REMOVED",
                "prod-1", 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_processesQuantityChangedAction() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent("cart-1", "user-1", "QUANTITY_CHANGED",
                "prod-1", 5, new BigDecimal("5.00"), 5, new BigDecimal("25.00"), java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_processesClearedAction() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent("cart-1", "user-1", "CLEARED",
                null, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_handlesUnknownAction() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent("cart-1", "user-1", "UNKNOWN_ACTION",
                null, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }

    @Test
    void handleCartUpdated_catchesExceptionsWithoutThrowing() {
        CartUpdatedEventHandler handler = new CartUpdatedEventHandler();
        var event = new com.example.shared.event.CartUpdatedEvent(null, null, "ITEM_ADDED",
                null, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, java.time.LocalDateTime.now());

        assertThatCode(() -> handler.handleCartUpdated(event)).doesNotThrowAnyException();
    }
}
