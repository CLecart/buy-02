package com.example.shared.service;

import com.example.shared.dto.SellerProfileDTO;
import com.example.shared.model.SellerProfile;
import com.example.shared.repository.SellerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SellerProfileServiceTest {

    @Mock
    private SellerProfileRepository profileRepository;

    @InjectMocks
    private SellerProfileService sellerProfileService;

    @Test
    void getOrCreateProfile_returnsExistingProfile() {
        SellerProfile profile = new SellerProfile();
        profile.setId("sp-1");
        profile.setSellerId("seller-1");
        profile.setStoreName("Store 1");
        profile.setTotalProductsSold(2);
        profile.setTotalRevenue(new BigDecimal("40.00"));

        when(profileRepository.findBySellerId("seller-1")).thenReturn(Optional.of(profile));

        SellerProfileDTO dto = sellerProfileService.getOrCreateProfile("seller-1");

        assertThat(dto.sellerId()).isEqualTo("seller-1");
        assertThat(dto.totalProductsSold()).isEqualTo(2);
        assertThat(dto.totalRevenue()).isEqualByComparingTo("40.00");
    }

    @Test
    void recordSale_createsAndUpdatesTotals() {
        when(profileRepository.findBySellerId("seller-new")).thenReturn(Optional.empty());
        when(profileRepository.save(any(SellerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sellerProfileService.recordSale("seller-new", 3, new BigDecimal("99.00"), "prod-1");

        ArgumentCaptor<SellerProfile> captor = ArgumentCaptor.forClass(SellerProfile.class);
        verify(profileRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        java.util.List<SellerProfile> savedProfiles = captor.getAllValues();
        SellerProfile saved = savedProfiles.get(savedProfiles.size() - 1);

        assertThat(saved.getSellerId()).isEqualTo("seller-new");
        assertThat(saved.getTotalProductsSold()).isEqualTo(3);
        assertThat(saved.getTotalRevenue()).isEqualByComparingTo("99.00");
        assertThat(saved.getBestSellingProductIds()).contains("prod-1");
        assertThat(saved.getSoldProductCounts()).containsEntry("prod-1", 3);
        assertThat(saved.getLastOrderDate()).isNotNull();
    }

    @Test
    void recordSale_handlesNullAndInvalidProductValues() {
        SellerProfile profile = new SellerProfile();
        profile.setSellerId("seller-2");
        profile.setTotalProductsSold(1);
        profile.setTotalRevenue(new BigDecimal("10.00"));
        profile.setBestSellingProductIds(new ArrayList<>());
        profile.setSoldProductCounts(new HashMap<>());

        when(profileRepository.findBySellerId("seller-2")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(SellerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sellerProfileService.recordSale("seller-2", null, null, " ");

        assertThat(profile.getTotalProductsSold()).isEqualTo(1);
        assertThat(profile.getTotalRevenue()).isEqualByComparingTo("10.00");
        assertThat(profile.getBestSellingProductIds()).isEmpty();
    }

    @Test
    void updateRating_throwsForOutOfRangeRating() {
        assertThatThrownBy(() -> sellerProfileService.updateRating("seller-1", 6.0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0.0 and 5.0");
    }

    @Test
    void updateRating_updatesExistingProfile() {
        SellerProfile profile = new SellerProfile();
        profile.setSellerId("seller-3");

        when(profileRepository.findBySellerId("seller-3")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(SellerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sellerProfileService.updateRating("seller-3", 4.7, 25);

        assertThat(profile.getAverageRating()).isEqualTo(4.7);
        assertThat(profile.getTotalReviews()).isEqualTo(25);
        verify(profileRepository).save(profile);
    }

    @Test
    void verifySeller_marksSellerAsVerified() {
        SellerProfile profile = new SellerProfile();
        profile.setSellerId("seller-4");
        profile.setVerified(false);

        when(profileRepository.findBySellerId("seller-4")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(SellerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sellerProfileService.verifySeller("seller-4");

        assertThat(profile.getVerified()).isTrue();
        verify(profileRepository).save(profile);
    }

    @Test
    void getProfile_throwsWhenMissing() {
        when(profileRepository.findBySellerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerProfileService.getProfile("missing"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Profile not found for seller");
    }
}
