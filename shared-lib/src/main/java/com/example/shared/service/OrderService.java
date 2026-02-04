package com.example.shared.service;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.dto.UpdateOrderStatusRequest;
import com.example.shared.model.Order;
import com.example.shared.model.OrderItem;
import com.example.shared.model.OrderStatus;
import com.example.shared.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Order management.
 * Handles order creation, status updates, and queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Order not found: ";

    private final OrderRepository orderRepository;
    private final UserProfileService userProfileService;
    private final SellerProfileService sellerProfileService;

    /**
     * Create a new order from a purchase request.
     *
     * @param request the order creation request
     * @return the created order DTO
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating new order for buyer: {}", request.buyerId());

        // Generate order ID
        String orderId = UUID.randomUUID().toString();

        // Calculate total price and create order items
        BigDecimal totalPrice = BigDecimal.ZERO;
        var items = request.items().stream()
                .map(itemRequest -> new OrderItem(
                        itemRequest.productId(),
                        itemRequest.sellerId(),
                        itemRequest.productName(),
                        itemRequest.quantity(),
                        itemRequest.price()
                ))
                .toList();

        for (var item : items) {
            totalPrice = totalPrice.add(item.getSubtotal());
        }

        // Create order
        Order order = new Order();
        order.setId(orderId);
        order.setBuyerId(request.buyerId());
        order.setItems(items);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.paymentMethod());
        order.setShippingAddress(request.shippingAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Update user profile with new order
        userProfileService.recordNewOrder(request.buyerId(), totalPrice);

        // Update seller profiles with new sales
        items.forEach(item -> sellerProfileService.recordSale(item.getSellerId(), item.getQuantity(), item.getSubtotal()));

        log.info("Order created successfully: {}", orderId);
        return mapToDTO(savedOrder);
    }

    /**
     * Get order by ID.
     *
     * @param orderId the order ID
     * @return the order DTO
     * @throws NoSuchElementException if order not found
     */
    public OrderDTO getOrderById(String orderId) {
        log.debug("Fetching order: {}", orderId);
        return orderRepository.findById(orderId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));
    }

    /**
     * Get all orders for a buyer with pagination.
     *
     * @param buyerId the buyer's user ID
     * @param pageable pagination info
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersByBuyer(String buyerId, Pageable pageable) {
        log.debug("Fetching orders for buyer: {}", buyerId);
        return orderRepository.findByBuyerId(buyerId, pageable).map(this::mapToDTO);
    }

    /**
     * Get all orders for a seller (where seller has items).
     *
     * @param sellerId the seller's user ID
     * @param pageable pagination info
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersBySeller(String sellerId, Pageable pageable) {
        log.debug("Fetching orders for seller: {}", sellerId);
        return orderRepository.findByItemsSellerId(sellerId, pageable).map(this::mapToDTO);
    }

    /**
     * Get orders by status.
     *
     * @param status the order status
     * @param pageable pagination info
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status, pageable).map(this::mapToDTO);
    }

    /**
     * Get orders by buyer and status.
     *
     * @param buyerId the buyer's user ID
     * @param status the order status
     * @param pageable pagination info
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersByBuyerAndStatus(String buyerId, OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders for buyer {} with status {}", buyerId, status);
        return orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable).map(this::mapToDTO);
    }

    /**
     * Update order status and optionally set tracking number.
     *
     * @param orderId the order ID
     * @param request status update request
     * @return updated order DTO
     */
    @Transactional
    public OrderDTO updateOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status - Order: {}, Status: {}", orderId, request.status());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.trackingNumber() != null && !request.trackingNumber().isBlank()) {
            order.setTrackingNumber(request.trackingNumber());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {}", orderId);
        return mapToDTO(updatedOrder);
    }

    /**
     * Cancel an order.
     *
     * @param orderId the order ID
     * @return updated order DTO
     */
    @Transactional
    public OrderDTO cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", orderId);
        return mapToDTO(cancelledOrder);
    }

    /**
     * Count total orders for a buyer.
     *
     * @param buyerId the buyer's user ID
     * @return order count
     */
    public long countOrdersByBuyer(String buyerId) {
        return orderRepository.countByBuyerId(buyerId);
    }

    /**
     * Count total orders by status.
     *
     * @param status the order status
     * @return order count
     */
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Map Order entity to OrderDTO.
     */
    private OrderDTO mapToDTO(Order order) {
        var itemDTOs = order.getItems().stream()
                .map(item -> new OrderDTO.OrderItemDTO(
                        item.getProductId(),
                        item.getSellerId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getBuyerId(),
                itemDTOs,
                order.getTotalPrice(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getShippingAddress(),
                order.getTrackingNumber(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
