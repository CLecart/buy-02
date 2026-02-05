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
import com.example.shared.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for Order management.
 * Handles order creation, status updates, and queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Order not found: ";
    private static final String PAGEABLE_REQUIRED = "pageable";
    private static final String QUERY_REQUIRED = "query";
    private static final String CRITERIA_REQUIRED = "criteria";

    private final OrderRepository orderRepository;
    private final UserProfileService userProfileService;
    private final SellerProfileService sellerProfileService;
    private final EventProducer eventProducer;
    private final MongoTemplate mongoTemplate;

    /**
     * Create a new order from a purchase request.
     *
     * @param request the order creation request
     * @return the created order DTO
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        return createOrderInternal(request);
    }

    private OrderDTO createOrderInternal(CreateOrderRequest request) {
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
        order.setBuyerEmail(request.buyerEmail());
        order.setItems(items);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.paymentMethod());
        order.setShippingAddress(request.shippingAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Update user profile with new order
        userProfileService.recordNewOrder(request.buyerId(), totalPrice, items);

        // Update seller profiles with new sales
        items.forEach(item -> sellerProfileService.recordSale(
            item.getSellerId(),
            item.getQuantity(),
            item.getSubtotal(),
            item.getProductId()
        ));

        // Publish order created event for async processing
        eventProducer.publishOrderCreated(
            new OrderCreatedEvent(
                orderId,
                request.buyerId(),
                request.buyerEmail(),
                items.stream()
                    .map(item -> new OrderCreatedEvent.OrderItemSnapshot(
                        item.getProductId(),
                        item.getSellerId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                    ))
                    .toList(),
                totalPrice,
                request.shippingAddress(),
                LocalDateTime.now()
            )
        );

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
    public OrderDTO getOrderById(@NonNull String orderId) {
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
        return getOrdersByBuyer(buyerId, pageable, null, null);
    }

    /**
     * Get all orders for a seller (where seller has items).
     *
     * @param sellerId the seller's user ID
     * @param pageable pagination info
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersBySeller(String sellerId, Pageable pageable) {
        return getOrdersBySeller(sellerId, pageable, null, null);
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
     * Search buyer orders by keyword and optional status.
     */
    public Page<OrderDTO> getOrdersByBuyer(String buyerId, Pageable pageable, String search, OrderStatus status) {
        log.debug("Searching orders for buyer {}", buyerId);
        Pageable safePageable = java.util.Objects.requireNonNull(pageable, PAGEABLE_REQUIRED);
        Query query = buildBuyerSearchQuery(buyerId, search, status).with(safePageable);
        return findOrders(query, safePageable);
    }

    /**
     * Search seller orders by keyword and optional status.
     */
    public Page<OrderDTO> getOrdersBySeller(String sellerId, Pageable pageable, String search, OrderStatus status) {
        log.debug("Searching orders for seller {}", sellerId);
        Pageable safePageable = java.util.Objects.requireNonNull(pageable, PAGEABLE_REQUIRED);
        Query query = buildSellerSearchQuery(sellerId, search, status).with(safePageable);
        return findOrders(query, safePageable);
    }

    /**
     * Update order status and optionally set tracking number.
     *
     * @param orderId the order ID
     * @param request status update request
     * @return updated order DTO
     */
    @Transactional
    public OrderDTO updateOrderStatus(@NonNull String orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status - Order: {}, Status: {}", orderId, request.status());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.trackingNumber() != null && !request.trackingNumber().isBlank()) {
            order.setTrackingNumber(request.trackingNumber());
        }

        Order updatedOrder = orderRepository.save(order);

        // Publish order status changed event
        eventProducer.publishOrderStatusChanged(
            new OrderStatusChangedEvent(
                orderId,
                order.getBuyerId(),
                order.getBuyerEmail(),
                oldStatus,
                request.status(),
                request.reason(),
                LocalDateTime.now()
            )
        );

        log.info("Order status updated successfully: {}", orderId);
        return mapToDTO(updatedOrder);
    }

    /**
     * Cancel an order for a buyer.
     */
    @Transactional
    public OrderDTO cancelOrder(@NonNull String orderId, String requesterId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        if (requesterId != null && !requesterId.equals(order.getBuyerId())) {
            throw new UnauthorizedException("Not allowed to cancel this order");
        }

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
     * Delete an order for a buyer.
     */
    @Transactional
    public void deleteOrder(@NonNull String orderId, String buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        if (buyerId != null && !buyerId.equals(order.getBuyerId())) {
            throw new UnauthorizedException("Not allowed to remove this order");
        }

        orderRepository.deleteById(orderId);
    }

    /**
     * Re-create an order from an existing one.
     */
    @Transactional
    public OrderDTO redoOrder(@NonNull String orderId, String buyerId) {
        Order original = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(ORDER_NOT_FOUND_MSG + orderId));

        if (buyerId != null && !buyerId.equals(original.getBuyerId())) {
            throw new UnauthorizedException("Not allowed to redo this order");
        }

        CreateOrderRequest request = new CreateOrderRequest(
                original.getBuyerId(),
                original.getBuyerEmail(),
                original.getItems().stream()
                        .map(item -> new CreateOrderRequest.OrderItemRequest(
                                item.getProductId(),
                                item.getSellerId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList(),
                original.getPaymentMethod(),
                original.getShippingAddress()
        );

        return createOrderInternal(request);
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

    private Page<OrderDTO> findOrders(Query query, Pageable pageable) {
        Query safeQuery = java.util.Objects.requireNonNull(query, QUERY_REQUIRED);
        Pageable safePageable = java.util.Objects.requireNonNull(pageable, PAGEABLE_REQUIRED);
        long total = mongoTemplate.count(Query.of(safeQuery).limit(-1).skip(-1), Order.class);
        var orders = mongoTemplate.find(safeQuery, Order.class);
        var dtos = java.util.Objects.requireNonNull(orders).stream().map(this::mapToDTO).toList();
        return new PageImpl<>(java.util.Objects.requireNonNull(dtos), safePageable, total);
    }

    private Query buildBuyerSearchQuery(String buyerId, String search, OrderStatus status) {
        Criteria criteria = Criteria.where("buyerId").is(buyerId);
        return buildSearchQuery(criteria, search, status);
    }

    private Query buildSellerSearchQuery(String sellerId, String search, OrderStatus status) {
        Criteria criteria = Criteria.where("items.sellerId").is(sellerId);
        return buildSearchQuery(criteria, search, status);
    }

    private Query buildSearchQuery(Criteria base, String search, OrderStatus status) {
        Criteria criteria = base;

        if (status != null) {
            criteria = criteria.and("status").is(status);
        }

        if (search != null && !search.isBlank()) {
            String pattern = ".*" + java.util.regex.Pattern.quote(search.trim()) + ".*";
            Criteria textCriteria = new Criteria().orOperator(
                    Criteria.where("id").regex(pattern, "i"),
                    Criteria.where("items.productName").regex(pattern, "i"),
                    Criteria.where("items.productId").regex(pattern, "i")
            );
            criteria = new Criteria().andOperator(criteria, textCriteria);
        }

        return new Query(java.util.Objects.requireNonNull(criteria, CRITERIA_REQUIRED));
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
            order.getBuyerEmail(),
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
