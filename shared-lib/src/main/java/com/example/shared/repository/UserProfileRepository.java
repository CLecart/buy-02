package com.example.shared.repository;

import com.example.shared.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * MongoDB repository for UserProfile entity.
 * Provides CRUD operations and queries for customer profiles.
 */
@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    /**
     * Find user profile by user ID.
     *
     * @param userId the user's ID
     * @return optional user profile
     */
    Optional<UserProfile> findByUserId(String userId);

    /**
     * Check if a profile exists for a user.
     *
     * @param userId the user's ID
     * @return true if profile exists
     */
    boolean existsByUserId(String userId);

    /**
     * Find top spenders (by total spent) with pagination.
     *
     * @param pageable pagination information (use Sort.by("totalSpent").descending())
     * @return page of user profiles
     */
    @Query("{ 'totalSpent': { $exists: true } }")
    Page<UserProfile> findTopSpenders(Pageable pageable);

    /**
     * Find users with total spent greater than a threshold.
     *
     * @param threshold minimum total spent
     * @param pageable pagination information
     * @return page of user profiles
     */
    Page<UserProfile> findByTotalSpentGreaterThan(BigDecimal threshold, Pageable pageable);

    /**
     * Find users with specific number of orders.
     *
     * @param minOrders minimum number of orders
     * @param pageable pagination information
     * @return page of user profiles
     */
    Page<UserProfile> findByTotalOrdersGreaterThanEqual(Integer minOrders, Pageable pageable);

    /**
     * Delete user profile by user ID.
     *
     * @param userId the user's ID
     */
    void deleteByUserId(String userId);
}
