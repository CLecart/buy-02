package com.example.productservice.dto;

import java.math.BigDecimal;

public class ProductSearchRequest {
    public record SortOptions(String sortBy, String sortDir) {
    }

    private final String search;
    private final String category;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String sellerId;
    private final Boolean inStock;
    private final SortOptions sortOptions;

    public ProductSearchRequest(
            String search,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sellerId,
            Boolean inStock,
            SortOptions sortOptions
    ) {
        this.search = search;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sellerId = sellerId;
        this.inStock = inStock;
        this.sortOptions = sortOptions;
    }

    public String getSearch() {
        return search;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public String getSellerId() {
        return sellerId;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public String getSortBy() {
        return sortOptions != null ? sortOptions.sortBy() : null;
    }

    public String getSortDir() {
        return sortOptions != null ? sortOptions.sortDir() : null;
    }
}
