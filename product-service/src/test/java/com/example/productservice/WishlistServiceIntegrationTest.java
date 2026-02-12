package com.example.productservice;

import com.example.productservice.model.Wishlist;
import com.example.productservice.model.WishlistItem;
import com.example.productservice.repository.WishlistRepository;
import com.example.productservice.service.WishlistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
class WishlistServiceIntegrationTest {

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private WishlistRepository wishlistRepository;

    private final String userId = "user-test";

    @BeforeEach
    void setUp() {
        wishlistRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        wishlistRepository.deleteAll();
    }

    @Test
    void testAddAndRemoveWishlistItem() {
        wishlistService.addToWishlist(userId, "prod-1");
        Wishlist wishlist = wishlistService.getWishlist(userId);
        Assertions.assertEquals(1, wishlist.getItems().size());
        WishlistItem item = wishlist.getItems().get(0);
        Assertions.assertEquals("prod-1", item.getProductId());

        wishlistService.removeFromWishlist(userId, "prod-1");
        wishlist = wishlistService.getWishlist(userId);
        Assertions.assertTrue(wishlist.getItems().isEmpty());
    }

    @Test
    void testClearWishlist() {
        wishlistService.addToWishlist(userId, "prod-1");
        wishlistService.addToWishlist(userId, "prod-2");
        Wishlist wishlist = wishlistService.getWishlist(userId);
        Assertions.assertEquals(2, wishlist.getItems().size());

        wishlistService.clearWishlist(userId);
        wishlist = wishlistService.getWishlist(userId);
        Assertions.assertTrue(wishlist.getItems().isEmpty());
    }

    @Test
    void testUniqueWishlistPerUser() {
        wishlistService.addToWishlist(userId, "prod-1");
        wishlistService.addToWishlist(userId, "prod-2");
        Wishlist wishlist = wishlistService.getWishlist(userId);
        Assertions.assertEquals(2, wishlist.getItems().size());

        Wishlist wishlist2 = wishlistService.getWishlist(userId);
        Assertions.assertEquals(wishlist.getId(), wishlist2.getId());
    }
}
