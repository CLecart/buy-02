package com.example.productservice.controller;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import com.example.productservice.dto.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.net.URI;

/**
 * Controller exposing product CRUD operations.
 *
 * <p>Ownership: the authenticated user (JWT subject) is considered the owner when creating
 * a product. Any owner information provided by a client in the request body is ignored by the
 * server and replaced with the JWT subject. This enforces server-side ownership and prevents
 * clients from forging ownership data.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@org.springframework.validation.annotation.Validated @org.springframework.web.bind.annotation.RequestBody ProductDto req) {
        /**
         * Create a product. The owner is taken from the authenticated JWT subject.
         *
         * @param req product DTO (ownerId field is ignored)
         * @return 201 Created with ProductDto containing the server-assigned ownerId
         */
        String userId = getCurrentUserId();
        Product saved = productService.createProduct(req, userId);
        ProductDto resp = toDto(saved);
        String id = java.util.Objects.requireNonNull(saved.getId());
        java.net.URI uri = java.util.Objects.requireNonNull(URI.create("/api/products/" + id));
        return ResponseEntity.created(uri).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@PathVariable("id") @org.springframework.lang.NonNull String id) {
        String resolvedId = java.util.Objects.requireNonNull(id);
        Product p = productService.getById(resolvedId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDto(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable("id") @org.springframework.lang.NonNull String id, @org.springframework.validation.annotation.Validated @org.springframework.web.bind.annotation.RequestBody ProductDto req) {
        String resolvedId = java.util.Objects.requireNonNull(id);
        String userId = getCurrentUserId();
        Product saved = productService.updateProduct(resolvedId, req, userId);
        if (saved == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @org.springframework.lang.NonNull String id) {
        String resolvedId = java.util.Objects.requireNonNull(id);
        String userId = getCurrentUserId();
        boolean ok = productService.deleteProduct(resolvedId, userId);
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "category", required = false) String category,
        @RequestParam(name = "minPrice", required = false) java.math.BigDecimal minPrice,
        @RequestParam(name = "maxPrice", required = false) java.math.BigDecimal maxPrice,
        @RequestParam(name = "sellerId", required = false) String sellerId,
        @RequestParam(name = "inStock", required = false) Boolean inStock
    ) {
        ProductSearchRequest filter = new ProductSearchRequest(search, category, minPrice, maxPrice, sellerId, inStock);
        Page<ProductDto> p = productService.listProducts(page, size, filter);
        return ResponseEntity.ok(p);
    }

    /**
     * Add a media ID to a product. Only the owner can add media.
     */
    @PostMapping("/{id}/media/{mediaId}")
    public ResponseEntity<ProductDto> addMedia(@PathVariable("id") String id, @PathVariable("mediaId") String mediaId) {
        String userId = getCurrentUserId();
        Product p = productService.addMediaToProduct(id, mediaId, userId);
        if (p == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(toDto(p));
    }

    /**
     * Remove a media ID from a product. Only the owner can remove media.
     */
    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<ProductDto> removeMedia(@PathVariable("id") String id, @PathVariable("mediaId") String mediaId) {
        String userId = getCurrentUserId();
        Product p = productService.removeMediaFromProduct(id, mediaId, userId);
        if (p == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(toDto(p));
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "";
        return auth.getName();
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getOwnerId(), p.getMediaIds());
        dto.setCategory(p.getCategory());
        dto.setQuantity(p.getQuantity());
        return dto;
    }
}
