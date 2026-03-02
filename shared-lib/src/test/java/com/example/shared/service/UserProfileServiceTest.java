package com.example.shared.service;

import com.example.shared.dto.UserProfileDTO;
import com.example.shared.model.OrderItem;
import com.example.shared.model.UserProfile;
import com.example.shared.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void getOrCreateProfile_returnsExistingProfile() {
        UserProfile profile = new UserProfile();
        profile.setId("p-1");
        profile.setUserId("u-1");
        profile.setTotalOrders(3);
        profile.setTotalSpent(new BigDecimal("30.00"));
        profile.setAverageOrderValue(new BigDecimal("10.00"));

        when(profileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));

        UserProfileDTO dto = userProfileService.getOrCreateProfile("u-1");

        assertThat(dto.userId()).isEqualTo("u-1");
        assertThat(dto.totalOrders()).isEqualTo(3);
        assertThat(dto.totalSpent()).isEqualByComparingTo("30.00");
    }

    @Test
    void getOrCreateProfile_createsProfileWhenMissing() {
        when(profileRepository.findByUserId("u-new")).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDTO dto = userProfileService.getOrCreateProfile("u-new");

        assertThat(dto.userId()).isEqualTo("u-new");
        assertThat(dto.totalOrders()).isZero();
        assertThat(dto.totalSpent()).isNotNull();
        assertThat(dto.totalSpent().signum()).isZero();
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void recordNewOrder_updatesTotalsAverageAndMostPurchased() {
        UserProfile profile = new UserProfile();
        profile.setId("p-2");
        profile.setUserId("u-2");
        profile.setTotalOrders(1);
        profile.setTotalSpent(new BigDecimal("20.00"));
        profile.setAverageOrderValue(new BigDecimal("20.00"));
        profile.setFavoriteProductIds(new ArrayList<>());
        profile.setMostPurchasedProductIds(new ArrayList<>());
        profile.setPurchasedProductCounts(new HashMap<>());

        List<OrderItem> items = List.of(
                new OrderItem("prod-1", "seller-1", "P1", 2, new BigDecimal("5.00")),
                new OrderItem("prod-2", "seller-2", "P2", 1, new BigDecimal("10.00"))
        );

        when(profileRepository.findByUserId("u-2")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.recordNewOrder("u-2", new BigDecimal("20.00"), items);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        UserProfile saved = captor.getValue();

        assertThat(saved.getTotalOrders()).isEqualTo(2);
        assertThat(saved.getTotalSpent()).isEqualByComparingTo("40.00");
        assertThat(saved.getAverageOrderValue()).isEqualByComparingTo("20.00");
        assertThat(saved.getPurchasedProductCounts()).containsEntry("prod-1", 2).containsEntry("prod-2", 1);
        assertThat(saved.getMostPurchasedProductIds()).contains("prod-1", "prod-2");
        assertThat(saved.getLastOrderDate()).isNotNull();
    }

    @Test
    void addFavoriteProduct_addsOnlyOnce() {
        UserProfile profile = new UserProfile();
        profile.setUserId("u-fav");
        profile.setFavoriteProductIds(new ArrayList<>());

        when(profileRepository.findByUserId("u-fav")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.addFavoriteProduct("u-fav", "prod-1");
        userProfileService.addFavoriteProduct("u-fav", "prod-1");

        assertThat(profile.getFavoriteProductIds()).containsExactly("prod-1");
        verify(profileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void removeFavoriteProduct_throwsWhenProfileMissing() {
        when(profileRepository.findByUserId("u-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.removeFavoriteProduct("u-missing", "prod-1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Profile not found");
    }

    @Test
    void removeFavoriteProduct_removesAndSaves() {
        UserProfile profile = new UserProfile();
        profile.setUserId("u-rm");
        profile.setFavoriteProductIds(new ArrayList<>(List.of("prod-1", "prod-2")));
        profile.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(profileRepository.findByUserId("u-rm")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.removeFavoriteProduct("u-rm", "prod-1");

        assertThat(profile.getFavoriteProductIds()).containsExactly("prod-2");
        verify(profileRepository).save(any(UserProfile.class));
    }
}
