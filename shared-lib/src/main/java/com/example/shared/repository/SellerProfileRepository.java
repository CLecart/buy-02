package com.example.shared.repository;

import com.example.shared.model.SellerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * MongoDB repository for SellerProfile entity.
 * Provides CRUD operations and queries for seller profiles.
 */
@Repository
public interface SellerProfileRepository extends MongoRepository<SellerProfile, String> {

    /**
     * Find seller profile by seller ID.
     *
     * @param sellerId the seller's user ID
     * @return optional seller profile
     */
    Optional<SellerProfile> findBySellerId(String sellerId);

    /**
     * Check if a profile exists for a seller.
     *
     * @param sellerId the seller's ID
     * @return true if profile exists
     */
    boolean existsBySellerId(String sellerId);

    /**
     * Find top sellers by revenue.
     *
     * @param pageable pagination information (use Sort.by("totalRevenue").descending())
     * @return page of seller profiles
     */
    @Query("{ 'totalRevenue': { $exists: true } }")
    Page<SellerProfile> findTopSellersByRevenue(Pageable pageable);

    /**
     * Find sellers by minimum rating.
     *
     * @param minRating minimum average rating
     * @param pageable pagination information
     * @return page of seller profiles
     */
    Page<SellerProfile> findByAverageRatingGreaterThanEqual(Double minRating, Pageable pageable);

    /**
     * Find sellers by revenue range.
     *
     * @param minRevenue minimum revenue
     * @param maxRevenue maximum revenue
     * @param pageable pagination information
     * @return page of seller profiles
     */
    Page<SellerProfile> findByTotalRevenueBetween(
            BigDecimal minRevenue,
            BigDecimal maxRevenue,
            Pageable pageable
    );

    /**
     * Find verified sellers.
     *
     * @param verified verification status
     * @param pageable pagination information
     * @return page of seller profiles
     */
    Page<SellerProfile> findByVerified(Boolean verified, Pageable pageable);

    /**
     * Delete seller profile by seller ID.
     *
     * @param sellerId the seller's ID
     */
    void deleteBySellerId(String sellerId);
}
