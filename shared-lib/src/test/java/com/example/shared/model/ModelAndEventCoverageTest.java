package com.example.shared.model;

import com.example.shared.dto.AuthRequest;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.UserDto;
import com.example.shared.kafka.event.ProductCreatedEvent;
import com.example.shared.kafka.event.ProductDeletedEvent;
import com.example.shared.kafka.event.ProductEvent;
import com.example.shared.kafka.event.ProductUpdatedEvent;
import com.example.shared.kafka.event.UserDeletedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("null")
class ModelAndEventCoverageTest {

    @Test
    void userDto_constructorWithRoles_setsPrimaryRole() {
        UserDto dto = new UserDto("u1", "Alice", "alice@test.com", "avatar.png", List.of("SELLER", "BUYER"));

        assertThat(dto.getId()).isEqualTo("u1");
        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getEmail()).isEqualTo("alice@test.com");
        assertThat(dto.getAvatarUrl()).isEqualTo("avatar.png");
        assertThat(dto.getRoles()).containsExactly("SELLER", "BUYER");
        assertThat(dto.getRole()).isEqualTo("SELLER");
    }

    @Test
    void userDto_constructorWithEnumLikeRole_mapsRoleAndRoles() {
        Object role = new Object() {
            @Override
            public String toString() {
                return "ADMIN";
            }
        };

        UserDto dto = new UserDto("u2", "Bob", "bob@test.com", role, "a.png");

        assertThat(dto.getRole()).isEqualTo("ADMIN");
        assertThat(dto.getRoles()).containsExactly("ADMIN");
    }

    @Test
    void userDto_settersAndGetters_work() {
        UserDto dto = new UserDto();
        dto.setId("u3");
        dto.setName("Carol");
        dto.setEmail("carol@test.com");
        dto.setAvatarUrl("c.png");
        dto.setRole("BUYER");
        dto.setRoles(List.of("BUYER"));

        assertThat(dto.getId()).isEqualTo("u3");
        assertThat(dto.getName()).isEqualTo("Carol");
        assertThat(dto.getEmail()).isEqualTo("carol@test.com");
        assertThat(dto.getAvatarUrl()).isEqualTo("c.png");
        assertThat(dto.getRole()).isEqualTo("BUYER");
        assertThat(dto.getRoles()).containsExactly("BUYER");
    }

    @Test
    void authRequest_and_authResponse_gettersSetters() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@test.com");
        req.setPassword("secret");

        AuthResponse resp = new AuthResponse();
        resp.setToken("jwt-token");
        resp.setExpiresInMs(3600000L);

        assertThat(req.getEmail()).isEqualTo("user@test.com");
        assertThat(req.getPassword()).isEqualTo("secret");
        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getExpiresInMs()).isEqualTo(3600000L);
    }

    @Test
    void userProfile_recordNewOrder_updatesTotalsAndAverage() {
        UserProfile profile = new UserProfile("user-1");

        profile.recordNewOrder(new BigDecimal("10.00"));
        profile.recordNewOrder(new BigDecimal("20.00"));

        assertThat(profile.getTotalOrders()).isEqualTo(2);
        assertThat(profile.getTotalSpent()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(profile.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(profile.getLastOrderDate()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    void userProfile_fullName_and_toString() {
        UserProfile profile = new UserProfile("u2");
        profile.setFirstName("Ada");
        profile.setLastName("Lovelace");

        assertThat(profile.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(profile.toString()).contains("userId='u2'");

        profile.setFirstName(null);
        profile.setLastName("Solo");
        assertThat(profile.getFullName()).isEqualTo("Solo");
    }

    @Test
    void orderItem_recalculateSubtotal_onQuantityAndPriceChange() {
        OrderItem item = new OrderItem("p1", "s1", "Prod", 2, new BigDecimal("7.50"));

        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("15.00"));

        item.setQuantity(3);
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("22.50"));

        item.setPrice(new BigDecimal("10.00"));
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("30.00"));

        item.setMediaId("m1");
        assertThat(item.getMediaId()).isEqualTo("m1");
        assertThat(item.toString()).contains("productId='p1'");
    }

    @Test
    void cartItem_recalculateSubtotal_and_constructorWithSubtotal() {
        CartItem item = new CartItem("p1", "s1", "Prod", 2, new BigDecimal("5.00"));

        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(item.getCreatedAt()).isNotNull();

        item.setQuantity(4);
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("20.00"));

        item.setPrice(new BigDecimal("6.00"));
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("24.00"));

        Instant now = Instant.now();
        java.time.LocalDateTime created = java.time.LocalDateTime.ofInstant(now, java.time.ZoneOffset.UTC);
        CartItem fullCtor = new CartItem("p2", "s2", "Prod2", 1, new BigDecimal("3.00"),
                new BigDecimal("3.00"), created);
        assertThat(fullCtor.getSubtotal()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(fullCtor.toString()).contains("productId='p2'");
    }

    @Test
    void productEvents_constructors_getters_setters_toString() {
        ProductCreatedEvent created = new ProductCreatedEvent("p1", "s1", "Name", "Desc",
                new BigDecimal("12.34"), 5);
        assertThat(created.getEventType()).isEqualTo(ProductEvent.EventType.CREATED);
        assertThat(created.getProductId()).isEqualTo("p1");
        assertThat(created.getSellerId()).isEqualTo("s1");
        assertThat(created.getName()).isEqualTo("Name");
        assertThat(created.toString()).contains("productId='p1'");

        ProductUpdatedEvent updated = new ProductUpdatedEvent();
        updated.setProductId("p2");
        updated.setSellerId("s2");
        updated.setName("N2");
        updated.setDescription("D2");
        updated.setPrice(new BigDecimal("8.99"));
        updated.setQuantity(9);
        assertThat(updated.getEventType()).isEqualTo(ProductEvent.EventType.UPDATED);
        assertThat(updated.toString()).contains("productId='p2'");

        ProductDeletedEvent deleted = new ProductDeletedEvent("p3", "s3");
        assertThat(deleted.getEventType()).isEqualTo(ProductEvent.EventType.DELETED);
        assertThat(deleted.toString()).contains("productId='p3'");

        ProductEvent.EventType type = ProductEvent.EventType.valueOf("UPDATED");
        assertThat(type).isEqualTo(ProductEvent.EventType.UPDATED);
    }

    @Test
    void userDeletedEvent_constructors_setters_toString() {
        UserDeletedEvent event = new UserDeletedEvent("u1", "SELLER");
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getUserId()).isEqualTo("u1");
        assertThat(event.getUserRole()).isEqualTo("SELLER");

        Instant ts = Instant.now();
        event.setEventId("e1");
        event.setTimestamp(ts);
        event.setUserId("u2");
        event.setUserRole("BUYER");

        assertThat(event.getEventId()).isEqualTo("e1");
        assertThat(event.getTimestamp()).isEqualTo(ts);
        assertThat(event.getUserId()).isEqualTo("u2");
        assertThat(event.getUserRole()).isEqualTo("BUYER");
        assertThat(event.toString()).contains("eventId='e1'");
    }
}
