package com.example.productservice.dto;

import java.math.BigDecimal;

public class ProductSearchRequest {
    private final String search;
    private final String category;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String sellerId;
    private final Boolean inStock;
    private final String sortBy;
    private final String sortDir;

    public ProductSearchRequest(
            String search,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sellerId,
            Boolean inStock,
            String sortBy,
            String sortDir
    ) {
        this.search = search;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sellerId = sellerId;
        this.inStock = inStock;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
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
        return sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }
}
