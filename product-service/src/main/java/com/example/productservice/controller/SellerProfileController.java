package com.example.productservice.controller;

import com.example.shared.dto.SellerProfileDTO;
import com.example.shared.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for Seller Profile Management.
 * Provides endpoints for seller profile queries and updates.
 */
@RestController
@RequestMapping("/api/profiles/sellers")
@RequiredArgsConstructor
@Slf4j
public class SellerProfileController {

    private final SellerProfileService profileService;

    /**
     * Get seller profile by seller ID.
     * GET /api/profiles/sellers/{sellerId}
     *
     * @param sellerId the seller's ID
     * @return seller profile DTO
     */
    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerProfileDTO> getProfile(@PathVariable String sellerId) {
        log.info("GET /api/profiles/sellers/{} - Fetching profile", sellerId);
        SellerProfileDTO profile = profileService.getOrCreateProfile(sellerId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get top sellers by revenue.
     * GET /api/profiles/sellers/analytics/top-revenue
     *
     * @param pageable pagination info
     * @return page of seller profiles
     */
    @GetMapping("/analytics/top-revenue")
    public ResponseEntity<Page<SellerProfileDTO>> getTopSellersByRevenue(Pageable pageable) {
        log.info("GET /api/profiles/sellers/analytics/top-revenue - Fetching top sellers");
        Page<SellerProfileDTO> topSellers = profileService.getTopSellersByRevenue(pageable);
        return ResponseEntity.ok(topSellers);
    }

    /**
     * Get sellers by minimum rating.
     * GET /api/profiles/sellers/analytics/by-rating?minRating=X
     *
     * @param minRating minimum rating
     * @param pageable pagination info
     * @return page of seller profiles
     */
    @GetMapping("/analytics/by-rating")
    public ResponseEntity<Page<SellerProfileDTO>> getSellersByMinRating(
            @RequestParam Double minRating,
            Pageable pageable
    ) {
        log.info("GET /api/profiles/sellers/analytics/by-rating - Min rating: {}", minRating);
        Page<SellerProfileDTO> sellers = profileService.getSellersByMinRating(minRating, pageable);
        return ResponseEntity.ok(sellers);
    }

    /**
     * Get verified sellers.
     * GET /api/profiles/sellers/analytics/verified
     *
     * @param pageable pagination info
     * @return page of verified seller profiles
     */
    @GetMapping("/analytics/verified")
    public ResponseEntity<Page<SellerProfileDTO>> getVerifiedSellers(Pageable pageable) {
        log.info("GET /api/profiles/sellers/analytics/verified - Fetching verified sellers");
        Page<SellerProfileDTO> verifiedSellers = profileService.getVerifiedSellers(pageable);
        return ResponseEntity.ok(verifiedSellers);
    }

    /**
     * Get sellers by revenue range.
     * GET /api/profiles/sellers/analytics/by-revenue?minRevenue=X&maxRevenue=Y
     *
     * @param minRevenue minimum revenue
     * @param maxRevenue maximum revenue
     * @param pageable pagination info
     * @return page of seller profiles
     */
    @GetMapping("/analytics/by-revenue")
    public ResponseEntity<Page<SellerProfileDTO>> getSellersByRevenueRange(
            @RequestParam BigDecimal minRevenue,
            @RequestParam BigDecimal maxRevenue,
            Pageable pageable
    ) {
        log.info("GET /api/profiles/sellers/analytics/by-revenue - Range: {} to {}", minRevenue, maxRevenue);
        Page<SellerProfileDTO> sellers = profileService.getSellersByRevenueRange(minRevenue, maxRevenue, pageable);
        return ResponseEntity.ok(sellers);
    }

    /**
     * Verify a seller account.
     * POST /api/profiles/sellers/{sellerId}/verify
     *
     * @param sellerId the seller's ID
     * @return no content response
     */
    @PostMapping("/{sellerId}/verify")
    public ResponseEntity<Void> verifySeller(@PathVariable String sellerId) {
        log.info("POST /api/profiles/sellers/{}/verify - Verifying seller", sellerId);
        profileService.verifySeller(sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
