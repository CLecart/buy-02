package com.example.shared.repository;

import com.example.shared.model.Order;
import com.example.shared.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Order entity.
 * Provides CRUD operations and custom queries for order management.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    /**
     * Find all orders for a specific buyer with pagination.
     *
     * @param buyerId the buyer's user ID
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByBuyerId(String buyerId, Pageable pageable);

    /**
     * Find all orders containing products from a specific seller.
     *
     * @param sellerId the seller's user ID
     * @param pageable pagination information
     * @return page of orders
     */
    @Query("{ 'items.sellerId': ?0 }")
    Page<Order> findByItemsSellerId(String sellerId, Pageable pageable);

    /**
     * Find orders by status.
     *
     * @param status the order status
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Find orders by buyer and status.
     *
     * @param buyerId the buyer's user ID
     * @param status the order status
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByBuyerIdAndStatus(String buyerId, OrderStatus status, Pageable pageable);

    /**
     * Find orders created within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders by buyer within a date range.
     *
     * @param buyerId the buyer's user ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByBuyerIdAndCreatedAtBetween(
            String buyerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find order by tracking number.
     *
     * @param trackingNumber the tracking number
     * @return optional order
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);

    /**
     * Count orders by buyer.
     *
     * @param buyerId the buyer's user ID
     * @return count of orders
     */
    long countByBuyerId(String buyerId);

    /**
     * Count orders by status.
     *
     * @param status the order status
     * @return count of orders
     */
    long countByStatus(OrderStatus status);

    /**
     * Find orders by multiple statuses.
     *
     * @param statuses list of statuses
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);
}
