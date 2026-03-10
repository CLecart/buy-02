package com.example.productservice.controller;

import com.example.shared.dto.SellerProfileDTO;
import com.example.shared.exception.UnauthorizedException;
import com.example.shared.service.SellerProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

class SellerProfileControllerTest {

    @Mock
    private SellerProfileService profileService;

    private SellerProfileController controller;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new SellerProfileController(profileService);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void getMyProfile_requiresSellerRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null));

        assertThatThrownBy(() -> controller.getMyProfile())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Seller role required");
    }

    @Test
    void getMyProfile_returnsCurrentSellerProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "seller-1",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        SellerProfileDTO dto = sampleProfile("seller-1");
        Mockito.when(profileService.getOrCreateProfile("seller-1")).thenReturn(dto);

        var response = controller.getMyProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(profileService).getOrCreateProfile("seller-1");
    }

    @Test
    void verifySeller_deniesDifferentSeller() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "seller-1",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        assertThatThrownBy(() -> controller.verifySeller("seller-2"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not allowed to manage another seller profile");
    }

    @Test
    void verifySeller_allowsSelfForSellerRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "seller-1",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))));

        var response = controller.verifySeller("seller-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(profileService).verifySeller("seller-1");
    }

    private SellerProfileDTO sampleProfile(String sellerId) {
        return new SellerProfileDTO(
                "profile-1",
                sellerId,
                "My Store",
                "desc",
                10,
                new BigDecimal("120.00"),
                4.5,
                12,
                List.of("p1"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
