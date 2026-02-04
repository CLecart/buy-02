package com.example.shared.service;

import com.example.shared.dto.UserProfileDTO;
import com.example.shared.model.UserProfile;
import com.example.shared.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for User Profile management.
 * Handles customer profile creation, updates, and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    /**
     * Get or create user profile.
     *
     * @param userId the user's ID
     * @return the user profile DTO
     */
    public UserProfileDTO getOrCreateProfile(String userId) {
        log.debug("Getting or creating profile for user: {}", userId);

        return profileRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setId(UUID.randomUUID().toString());
                    newProfile.setUserId(userId);
                    newProfile.setTotalOrders(0);
                    newProfile.setTotalSpent(BigDecimal.ZERO);
                    newProfile.setAverageOrderValue(BigDecimal.ZERO);
                    newProfile.setFavoriteProductIds(Collections.emptyList());
                    newProfile.setMostPurchasedProductIds(Collections.emptyList());
                    newProfile.setCreatedAt(LocalDateTime.now());
                    newProfile.setUpdatedAt(LocalDateTime.now());

                    UserProfile savedProfile = profileRepository.save(newProfile);
                    log.info("New profile created for user: {}", userId);
                    return mapToDTO(savedProfile);
                });
    }

    /**
     * Get user profile by user ID.
     *
     * @param userId the user's ID
     * @return the user profile DTO
     */
    public UserProfileDTO getProfile(String userId) {
        log.debug("Fetching profile for user: {}", userId);
        return profileRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new NoSuchElementException("Profile not found for user: " + userId));
    }

    /**
     * Record a new order for a user.
     * Updates order count, total spent, and average order value.
     *
     * @param userId the user's ID
     * @param orderTotal the order total amount
     */
    @Transactional
    public void recordNewOrder(String userId, BigDecimal orderTotal) {
        log.debug("Recording new order for user: {} with total: {}", userId, orderTotal);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createNewProfile(userId));

        profile.setTotalOrders(profile.getTotalOrders() + 1);
        profile.setTotalSpent(profile.getTotalSpent().add(orderTotal));

        // Calculate new average
        BigDecimal newAverage = profile.getTotalSpent()
                .divide(BigDecimal.valueOf(profile.getTotalOrders()), 2, java.math.RoundingMode.HALF_UP);
        profile.setAverageOrderValue(newAverage);

        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
        log.debug("Order recorded successfully for user: {}", userId);
    }

    /**
     * Add product to user's favorites.
     *
     * @param userId the user's ID
     * @param productId the product ID
     */
    @Transactional
    public void addFavoriteProduct(String userId, String productId) {
        log.debug("Adding favorite product - User: {}, Product: {}", userId, productId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createNewProfile(userId));

        if (!profile.getFavoriteProductIds().contains(productId)) {
            profile.getFavoriteProductIds().add(productId);
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
            log.debug("Favorite product added successfully");
        }
    }

    /**
     * Remove product from user's favorites.
     *
     * @param userId the user's ID
     * @param productId the product ID
     */
    @Transactional
    public void removeFavoriteProduct(String userId, String productId) {
        log.debug("Removing favorite product - User: {}, Product: {}", userId, productId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found for user: " + userId));

        if (profile.getFavoriteProductIds().remove(productId)) {
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
            log.debug("Favorite product removed successfully");
        }
    }

    /**
     * Get top spenders.
     *
     * @param pageable pagination info
     * @return page of user profiles
     */
    public Page<UserProfileDTO> getTopSpenders(Pageable pageable) {
        log.debug("Fetching top spenders");
        return profileRepository.findTopSpenders(pageable).map(this::mapToDTO);
    }

    /**
     * Get users by minimum spending threshold.
     *
     * @param minSpent minimum total spent
     * @param pageable pagination info
     * @return page of user profiles
     */
    public Page<UserProfileDTO> getUsersByMinSpent(BigDecimal minSpent, Pageable pageable) {
        log.debug("Fetching users with min spent: {}", minSpent);
        return profileRepository.findByTotalSpentGreaterThan(minSpent, pageable).map(this::mapToDTO);
    }

    /**
     * Get users by minimum order count.
     *
     * @param minOrders minimum number of orders
     * @param pageable pagination info
     * @return page of user profiles
     */
    public Page<UserProfileDTO> getUsersByMinOrders(Integer minOrders, Pageable pageable) {
        log.debug("Fetching users with min orders: {}", minOrders);
        return profileRepository.findByTotalOrdersGreaterThanEqual(minOrders, pageable).map(this::mapToDTO);
    }

    /**
     * Create a new user profile.
     */
    private UserProfile createNewProfile(String userId) {
        UserProfile newProfile = new UserProfile();
        newProfile.setId(UUID.randomUUID().toString());
        newProfile.setUserId(userId);
        newProfile.setTotalOrders(0);
        newProfile.setTotalSpent(BigDecimal.ZERO);
        newProfile.setAverageOrderValue(BigDecimal.ZERO);
        newProfile.setFavoriteProductIds(Collections.emptyList());
        newProfile.setMostPurchasedProductIds(Collections.emptyList());
        newProfile.setCreatedAt(LocalDateTime.now());
        newProfile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(newProfile);
    }

    /**
     * Map UserProfile entity to UserProfileDTO.
     */
    private UserProfileDTO mapToDTO(UserProfile profile) {
        return new UserProfileDTO(
                profile.getId(),
                profile.getUserId(),
                profile.getTotalOrders(),
                profile.getTotalSpent(),
                profile.getAverageOrderValue(),
                profile.getFavoriteProductIds(),
                profile.getMostPurchasedProductIds(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
