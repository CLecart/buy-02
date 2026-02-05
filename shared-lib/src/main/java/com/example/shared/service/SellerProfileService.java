package com.example.shared.service;

import com.example.shared.dto.SellerProfileDTO;
import com.example.shared.model.SellerProfile;
import com.example.shared.repository.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for Seller Profile management.
 * Handles seller profile creation, updates, and revenue/rating management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SellerProfileService {

    private static final String PROFILE_NOT_FOUND_MSG = "Profile not found for seller: ";

    private final SellerProfileRepository profileRepository;

    /**
     * Get or create seller profile.
     *
     * @param sellerId the seller's ID
     * @return the seller profile DTO
     */
    public SellerProfileDTO getOrCreateProfile(String sellerId) {
        log.debug("Getting or creating profile for seller: {}", sellerId);

        return profileRepository.findBySellerId(sellerId)
                .map(this::mapToDTO)
                .orElseGet(() -> {
                    SellerProfile newProfile = createNewProfile(sellerId);
                    return mapToDTO(newProfile);
                });
    }

    /**
     * Get seller profile by seller ID.
     *
     * @param sellerId the seller's ID
     * @return the seller profile DTO
     */
    public SellerProfileDTO getProfile(String sellerId) {
        log.debug("Fetching profile for seller: {}", sellerId);
        return profileRepository.findBySellerId(sellerId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new NoSuchElementException(PROFILE_NOT_FOUND_MSG + sellerId));
    }

    /**
     * Record a new sale for a seller.
     * Updates product count and total revenue.
     *
     * @param sellerId the seller's ID
     * @param quantity quantity sold
     * @param revenue revenue from sale
     */
    @Transactional
    public void recordSale(String sellerId, Integer quantity, BigDecimal revenue, String productId) {
        log.debug("Recording sale for seller: {} - Qty: {}, Revenue: {}", sellerId, quantity, revenue);

        SellerProfile profile = profileRepository.findBySellerId(sellerId)
                .orElseGet(() -> createNewProfile(sellerId));

        int totalSold = profile.getTotalProductsSold() != null ? profile.getTotalProductsSold() : 0;
        BigDecimal totalRevenue = profile.getTotalRevenue() != null ? profile.getTotalRevenue() : BigDecimal.ZERO;
        int soldQty = quantity != null ? quantity : 0;
        BigDecimal saleRevenue = revenue != null ? revenue : BigDecimal.ZERO;

        profile.setTotalProductsSold(totalSold + soldQty);
        profile.setTotalRevenue(totalRevenue.add(saleRevenue));
        profile.setLastOrderDate(LocalDateTime.now());

        updateBestSellingProducts(profile, productId, quantity);
        profile.setUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);
        log.debug("Sale recorded successfully for seller: {}", sellerId);
    }

    /**
     * Update seller rating.
     *
     * @param sellerId the seller's ID
     * @param newRating the new rating (0.0 - 5.0)
     * @param reviewCount number of reviews
     */
    @Transactional
    public void updateRating(String sellerId, Double newRating, Integer reviewCount) {
        log.debug("Updating rating for seller: {} - Rating: {}", sellerId, newRating);

        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }

        SellerProfile profile = profileRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new NoSuchElementException(PROFILE_NOT_FOUND_MSG + sellerId));

        profile.setAverageRating(newRating);
        profile.setTotalReviews(reviewCount);
        profile.setUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);
        log.debug("Rating updated successfully for seller: {}", sellerId);
    }

    /**
     * Verify a seller account.
     *
     * @param sellerId the seller's ID
     */
    @Transactional
    public void verifySeller(String sellerId) {
        log.info("Verifying seller: {}", sellerId);

        SellerProfile profile = profileRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new NoSuchElementException(PROFILE_NOT_FOUND_MSG + sellerId));

        profile.setVerified(true);
        profile.setUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);
        log.info("Seller verified successfully: {}", sellerId);
    }

    /**
     * Get top sellers by revenue.
     *
     * @param pageable pagination info
     * @return page of seller profiles
     */
    public Page<SellerProfileDTO> getTopSellersByRevenue(Pageable pageable) {
        log.debug("Fetching top sellers by revenue");
        return profileRepository.findTopSellersByRevenue(pageable).map(this::mapToDTO);
    }

    /**
     * Get sellers by minimum rating.
     *
     * @param minRating minimum rating
     * @param pageable pagination info
     * @return page of seller profiles
     */
    public Page<SellerProfileDTO> getSellersByMinRating(Double minRating, Pageable pageable) {
        log.debug("Fetching sellers with min rating: {}", minRating);
        return profileRepository.findByAverageRatingGreaterThanEqual(minRating, pageable).map(this::mapToDTO);
    }

    /**
     * Get verified sellers.
     *
     * @param pageable pagination info
     * @return page of verified seller profiles
     */
    public Page<SellerProfileDTO> getVerifiedSellers(Pageable pageable) {
        log.debug("Fetching verified sellers");
        return profileRepository.findByVerified(true, pageable).map(this::mapToDTO);
    }

    /**
     * Get sellers by revenue range.
     *
     * @param minRevenue minimum revenue
     * @param maxRevenue maximum revenue
     * @param pageable pagination info
     * @return page of seller profiles
     */
    public Page<SellerProfileDTO> getSellersByRevenueRange(BigDecimal minRevenue, BigDecimal maxRevenue, Pageable pageable) {
        log.debug("Fetching sellers with revenue between {} and {}", minRevenue, maxRevenue);
        return profileRepository.findByTotalRevenueBetween(minRevenue, maxRevenue, pageable).map(this::mapToDTO);
    }

    /**
     * Create a new seller profile.
     */
    private SellerProfile createNewProfile(String sellerId) {
        SellerProfile newProfile = new SellerProfile();
        newProfile.setId(UUID.randomUUID().toString());
        newProfile.setSellerId(sellerId);
        newProfile.setStoreName("Store of " + sellerId);
        newProfile.setStoreDescription("");
        newProfile.setTotalProductsSold(0);
        newProfile.setTotalRevenue(BigDecimal.ZERO);
        newProfile.setAverageRating(0.0);
        newProfile.setTotalReviews(0);
        newProfile.setBestSellingProductIds(new ArrayList<>());
        newProfile.setSoldProductCounts(new HashMap<>());
        newProfile.setVerified(false);
        newProfile.setCreatedAt(LocalDateTime.now());
        newProfile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(newProfile);
    }

    private void updateBestSellingProducts(SellerProfile profile, String productId, Integer quantity) {
        if (productId == null || productId.isBlank() || quantity == null || quantity <= 0) {
            return;
        }

        Map<String, Integer> counts = profile.getSoldProductCounts();
        if (counts == null) {
            counts = new HashMap<>();
            profile.setSoldProductCounts(counts);
        }

        counts.merge(productId, quantity, (left, right) -> left + right);

        List<String> topProducts = counts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        profile.setBestSellingProductIds(new ArrayList<>(topProducts));
    }

    /**
     * Map SellerProfile entity to SellerProfileDTO.
     */
    private SellerProfileDTO mapToDTO(SellerProfile profile) {
        return new SellerProfileDTO(
                profile.getId(),
                profile.getSellerId(),
                profile.getStoreName(),
                profile.getStoreDescription(),
                profile.getTotalProductsSold(),
                profile.getTotalRevenue(),
                profile.getAverageRating(),
                profile.getTotalReviews(),
                profile.getBestSellingProductIds(),
                profile.getVerified(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
