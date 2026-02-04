package com.example.shared.repository;

import com.example.shared.model.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for ShoppingCart entity.
 * Provides CRUD operations for shopping cart management.
 */
@Repository
public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {

    /**
     * Find shopping cart by user ID.
     * Each user has exactly one cart.
     *
     * @param userId the user's ID
     * @return optional shopping cart
     */
    Optional<ShoppingCart> findByUserId(String userId);

    /**
     * Delete shopping cart by user ID.
     *
     * @param userId the user's ID
     */
    void deleteByUserId(String userId);

    /**
     * Check if a shopping cart exists for a user.
     *
     * @param userId the user's ID
     * @return true if cart exists
     */
    boolean existsByUserId(String userId);
}
