package com.example.orderservice.controller;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.dto.UpdateOrderStatusRequest;
import com.example.shared.model.OrderStatus;
import com.example.shared.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Order Management.
 * Provides endpoints for order CRUD operations and queries.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * POST /api/orders
     *
     * @param request order creation request
     * @return created order with 201 status
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders - Creating new order for buyer: {}", request.buyerId());
        OrderDTO createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Get order by ID.
     * GET /api/orders/{orderId}
     *
     * @param orderId the order ID
     * @return order DTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        log.info("GET /api/orders/{} - Fetching order", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders for a buyer.
     * GET /api/orders/buyer/{buyerId}
     *
     * @param buyerId the buyer's user ID
     * @param pageable pagination info
     * @return page of orders
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByBuyer(
            @PathVariable String buyerId,
            Pageable pageable
    ) {
        log.info("GET /api/orders/buyer/{} - Fetching orders for buyer", buyerId);
        Page<OrderDTO> orders = orderService.getOrdersByBuyer(buyerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all orders for a seller (containing their items).
     * GET /api/orders/seller/{sellerId}
     *
     * @param sellerId the seller's user ID
     * @param pageable pagination info
     * @return page of orders
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<OrderDTO>> getOrdersBySeller(
            @PathVariable String sellerId,
            Pageable pageable
    ) {
        log.info("GET /api/orders/seller/{} - Fetching orders for seller", sellerId);
        Page<OrderDTO> orders = orderService.getOrdersBySeller(sellerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status.
     * GET /api/orders/status/{status}
     *
     * @param status the order status
     * @param pageable pagination info
     * @return page of orders
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable
    ) {
        log.info("GET /api/orders/status/{} - Fetching orders with status", status);
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by buyer and status.
     * GET /api/orders/buyer/{buyerId}/status/{status}
     *
     * @param buyerId the buyer's user ID
     * @param status the order status
     * @param pageable pagination info
     * @return page of orders
     */
    @GetMapping("/buyer/{buyerId}/status/{status}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByBuyerAndStatus(
            @PathVariable String buyerId,
            @PathVariable OrderStatus status,
            Pageable pageable
    ) {
        log.info("GET /api/orders/buyer/{}/status/{} - Fetching orders", buyerId, status);
        Page<OrderDTO> orders = orderService.getOrdersByBuyerAndStatus(buyerId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status.
     * PATCH /api/orders/{orderId}/status
     *
     * @param orderId the order ID
     * @param request status update request
     * @return updated order
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        log.info("PATCH /api/orders/{}/status - Updating status to: {}", orderId, request.status());
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Cancel an order.
     * PATCH /api/orders/{orderId}/cancel
     *
     * @param orderId the order ID
     * @return cancelled order
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable String orderId) {
        log.info("PATCH /api/orders/{}/cancel - Cancelling order", orderId);
        OrderDTO cancelledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * Count orders for a buyer.
     * GET /api/orders/buyer/{buyerId}/count
     *
     * @param buyerId the buyer's user ID
     * @return order count
     */
    @GetMapping("/buyer/{buyerId}/count")
    public ResponseEntity<Long> countOrdersByBuyer(@PathVariable String buyerId) {
        log.info("GET /api/orders/buyer/{}/count - Counting orders", buyerId);
        long count = orderService.countOrdersByBuyer(buyerId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count orders by status.
     * GET /api/orders/status/{status}/count
     *
     * @param status the order status
     * @return order count
     */
    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> countOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/orders/status/{}/count - Counting orders", status);
        long count = orderService.countOrdersByStatus(status);
        return ResponseEntity.ok(count);
    }
}
