package com.example.productservice.service;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.kafka.ProductEventProducer;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public ProductService(ProductRepository productRepository,
                          ProductEventProducer productEventProducer) {
        this.productRepository = productRepository;
        this.productEventProducer = productEventProducer;
    }

    public Product createProduct(ProductDto dto, String ownerId) {
        Product p = new Product(dto.getName(), dto.getDescription(), dto.getPrice(), ownerId, dto.getQuantity());
        if (dto.getMediaIds() != null) {
            p.setMediaIds(dto.getMediaIds());
        }
        Product saved = productRepository.save(p);

        // Publish event for inter-service communication
        productEventProducer.publishProductCreated(
                saved.getId(), ownerId, saved.getName(),
                saved.getDescription(), saved.getPrice(), saved.getQuantity());

        return saved;
    }

    public Product getById(String id) {
        return productRepository.findById(java.util.Objects.requireNonNull(id)).orElse(null);
    }

    public Product updateProduct(String id, ProductDto dto, String ownerId) {
    var opt = productRepository.findById(java.util.Objects.requireNonNull(id));
        if (opt.isEmpty()) return null;
        Product existing = opt.get();
        if (!ownerId.equals(existing.getOwnerId())) return null;
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        if (dto.getMediaIds() != null) {
            existing.setMediaIds(dto.getMediaIds());
        }
        Product saved = productRepository.save(existing);

        // Publish event for inter-service communication
        productEventProducer.publishProductUpdated(
                saved.getId(), ownerId, saved.getName(),
                saved.getDescription(), saved.getPrice(), saved.getQuantity());

        return saved;
    }

    public boolean deleteProduct(String id, String ownerId) {
    var opt = productRepository.findById(java.util.Objects.requireNonNull(id));
        if (opt.isEmpty()) return false;
        Product existing = opt.get();
        if (!ownerId.equals(existing.getOwnerId())) return false;

        // Publish event BEFORE delete for media cleanup
        productEventProducer.publishProductDeleted(id, ownerId);

        productRepository.deleteById(java.util.Objects.requireNonNull(id));
        return true;
    }

    public Page<ProductDto> listProducts(int page, int size, String search) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        if (search == null || search.isBlank()) {
            var p = productRepository.findAll(pageable);
            java.util.List<ProductDto> dtos = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
            return new PageImpl<>(java.util.Objects.requireNonNull(dtos), pageable, p.getTotalElements());
        } else {
            var p = productRepository.findByNameContainingIgnoreCase(search, pageable);
            java.util.List<ProductDto> dtos = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
            return new PageImpl<>(java.util.Objects.requireNonNull(dtos), pageable, p.getTotalElements());
        }
    }

    public Product addMediaToProduct(String productId, String mediaId, String ownerId) {
        var opt = productRepository.findById(java.util.Objects.requireNonNull(productId));
        if (opt.isEmpty()) return null;
        Product existing = opt.get();
        if (!ownerId.equals(existing.getOwnerId())) return null;
        existing.addMediaId(mediaId);
        return productRepository.save(existing);
    }

    public Product removeMediaFromProduct(String productId, String mediaId, String ownerId) {
        var opt = productRepository.findById(java.util.Objects.requireNonNull(productId));
        if (opt.isEmpty()) return null;
        Product existing = opt.get();
        if (!ownerId.equals(existing.getOwnerId())) return null;
        existing.removeMediaId(mediaId);
        return productRepository.save(existing);
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getOwnerId(), p.getMediaIds());
        dto.setQuantity(p.getQuantity());
        return dto;
    }
}
