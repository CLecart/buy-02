package com.example.userservice.controller;

import com.example.shared.dto.UserProfileDTO;
import com.example.shared.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for User Profile Management.
 * Provides endpoints for customer profile queries and updates.
 */
@RestController
@RequestMapping("/api/profiles/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * Get user profile by user ID.
     * GET /api/profiles/users/{userId}
     *
     * @param userId the user's ID
     * @return user profile DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String userId) {
        log.info("GET /api/profiles/users/{} - Fetching profile", userId);
        UserProfileDTO profile = profileService.getOrCreateProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Add product to user's favorites.
     * POST /api/profiles/users/{userId}/favorites/{productId}
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @return no content response
     */
    @PostMapping("/{userId}/favorites/{productId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable String userId,
            @PathVariable String productId
    ) {
        log.info("POST /api/profiles/users/{}/favorites/{} - Adding favorite", userId, productId);
        profileService.addFavoriteProduct(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Remove product from user's favorites.
     * DELETE /api/profiles/users/{userId}/favorites/{productId}
     *
     * @param userId the user's ID
     * @param productId the product ID
     * @return no content response
     */
    @DeleteMapping("/{userId}/favorites/{productId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String userId,
            @PathVariable String productId
    ) {
        log.info("DELETE /api/profiles/users/{}/favorites/{} - Removing favorite", userId, productId);
        profileService.removeFavoriteProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get top spenders.
     * GET /api/profiles/users/analytics/top-spenders
     *
     * @param pageable pagination info
     * @return page of user profiles
     */
    @GetMapping("/analytics/top-spenders")
    public ResponseEntity<Page<UserProfileDTO>> getTopSpenders(Pageable pageable) {
        log.info("GET /api/profiles/users/analytics/top-spenders - Fetching top spenders");
        Page<UserProfileDTO> topSpenders = profileService.getTopSpenders(pageable);
        return ResponseEntity.ok(topSpenders);
    }

    /**
     * Get users by minimum spending.
     * GET /api/profiles/users/analytics/by-spending?minSpent=X
     *
     * @param minSpent minimum total spent
     * @param pageable pagination info
     * @return page of user profiles
     */
    @GetMapping("/analytics/by-spending")
    public ResponseEntity<Page<UserProfileDTO>> getUsersByMinSpent(
            @RequestParam BigDecimal minSpent,
            Pageable pageable
    ) {
        log.info("GET /api/profiles/users/analytics/by-spending - Min spent: {}", minSpent);
        Page<UserProfileDTO> users = profileService.getUsersByMinSpent(minSpent, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by minimum order count.
     * GET /api/profiles/users/analytics/by-orders?minOrders=X
     *
     * @param minOrders minimum number of orders
     * @param pageable pagination info
     * @return page of user profiles
     */
    @GetMapping("/analytics/by-orders")
    public ResponseEntity<Page<UserProfileDTO>> getUsersByMinOrders(
            @RequestParam Integer minOrders,
            Pageable pageable
    ) {
        log.info("GET /api/profiles/users/analytics/by-orders - Min orders: {}", minOrders);
        Page<UserProfileDTO> users = profileService.getUsersByMinOrders(minOrders, pageable);
        return ResponseEntity.ok(users);
    }
}
