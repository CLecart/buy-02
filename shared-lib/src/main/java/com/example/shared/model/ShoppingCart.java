package com.example.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * ShoppingCart entity representing a user's shopping cart.
 * Stored in MongoDB collection "shopping_carts".
 * One cart per user, updated frequently.
 */
@Document(collection = "shopping_carts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<CartItem> items = new ArrayList<>();

    private BigDecimal totalPrice = BigDecimal.ZERO;

    private Integer itemCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
