package com.example.productservice.service;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.ProductSearchRequest;
import com.example.productservice.kafka.ProductEventProducer;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

@Service
public class ProductService {

    private static final String FIELD_PRICE = "price";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_ID = "id";
    private static final String SORT_FIELD_ERROR = "Unsupported sortBy value. Allowed: name, price, quantity, category";
    private static final String SORT_DIR_ERROR = "Unsupported sortDir value. Allowed: asc, desc";

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;
    private final MongoTemplate mongoTemplate;

    public ProductService(ProductRepository productRepository,
                          ProductEventProducer productEventProducer,
                          MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.productEventProducer = productEventProducer;
        this.mongoTemplate = mongoTemplate;
    }

    public Product createProduct(ProductDto dto, String ownerId) {
        Product p = new Product(dto.getName(), dto.getDescription(), dto.getPrice(), ownerId, dto.getQuantity());
        p.setCategory(dto.getCategory());
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
        existing.setCategory(dto.getCategory());
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

    public Page<ProductDto> listProducts(int page, int size, ProductSearchRequest filter) {
        Sort resolvedSort = java.util.Objects.requireNonNull(resolveSort(filter));
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), resolvedSort);
        Query query = buildFilterQuery(filter).with(pageable);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class);
        List<Product> products = mongoTemplate.find(query, Product.class);
        List<ProductDto> dtos = products.stream().map(this::toDto).toList();
        return new PageImpl<>(java.util.Objects.requireNonNull(dtos), pageable, total);
    }

    private Sort resolveSort(ProductSearchRequest filter) {
        if (filter == null || filter.getSortBy() == null || filter.getSortBy().isBlank()) {
            return Sort.by(Sort.Direction.ASC, FIELD_ID);
        }

        String normalizedField = filter.getSortBy().trim().toLowerCase(Locale.ROOT);
        String sortField = switch (normalizedField) {
            case FIELD_NAME -> FIELD_NAME;
            case FIELD_PRICE -> FIELD_PRICE;
            case FIELD_QUANTITY -> FIELD_QUANTITY;
            case FIELD_CATEGORY -> FIELD_CATEGORY;
            default -> throw new IllegalArgumentException(SORT_FIELD_ERROR);
        };

        Sort.Direction direction = resolveDirection(filter.getSortDir());
        Sort primarySort = Sort.by(sortField);
        if (Sort.Direction.DESC.equals(direction)) {
            primarySort = primarySort.descending();
        } else {
            primarySort = primarySort.ascending();
        }

        return primarySort.and(Sort.by(FIELD_ID).ascending());
    }

    private Sort.Direction resolveDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.ASC;
        }
        String normalizedDirection = sortDir.trim().toLowerCase(Locale.ROOT);
        Sort.Direction direction = switch (normalizedDirection) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException(SORT_DIR_ERROR);
        };
        return java.util.Objects.requireNonNull(direction);
    }

    private Query buildFilterQuery(
            ProductSearchRequest filter
    ) {
        String search = filter != null ? filter.getSearch() : null;
        String category = filter != null ? filter.getCategory() : null;
        BigDecimal minPrice = filter != null ? filter.getMinPrice() : null;
        BigDecimal maxPrice = filter != null ? filter.getMaxPrice() : null;
        String sellerId = filter != null ? filter.getSellerId() : null;
        Boolean inStock = filter != null ? filter.getInStock() : null;
        List<Criteria> criteriaList = new ArrayList<>();

        addSearchCriteria(criteriaList, search);
        addCategoryCriteria(criteriaList, category);
        addSellerCriteria(criteriaList, sellerId);
        addPriceCriteria(criteriaList, minPrice, maxPrice);
        addStockCriteria(criteriaList, inStock);

        if (criteriaList.isEmpty()) {
            return new Query();
        }

        return new Query(new Criteria().andOperator(criteriaList));
    }

    private void addSearchCriteria(List<Criteria> criteriaList, String search) {
        if (search == null || search.isBlank()) return;
        String pattern = ".*" + java.util.regex.Pattern.quote(search.trim()) + ".*";
        criteriaList.add(new Criteria().orOperator(
            Criteria.where(FIELD_NAME).regex(pattern, "i"),
            Criteria.where("description").regex(pattern, "i"),
            Criteria.where(FIELD_CATEGORY).regex(pattern, "i")
        ));
    }

    private void addCategoryCriteria(List<Criteria> criteriaList, String category) {
        if (category == null || category.isBlank()) return;
        criteriaList.add(Criteria.where(FIELD_CATEGORY).is(category));
    }

    private void addSellerCriteria(List<Criteria> criteriaList, String sellerId) {
        if (sellerId == null || sellerId.isBlank()) return;
        criteriaList.add(Criteria.where("ownerId").is(sellerId));
    }

    private void addPriceCriteria(List<Criteria> criteriaList, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null) {
            criteriaList.add(Criteria.where(FIELD_PRICE).gte(minPrice).lte(maxPrice));
            return;
        }
        if (minPrice != null) {
            criteriaList.add(Criteria.where(FIELD_PRICE).gte(minPrice));
            return;
        }
        if (maxPrice != null) {
            criteriaList.add(Criteria.where(FIELD_PRICE).lte(maxPrice));
        }
    }

    private void addStockCriteria(List<Criteria> criteriaList, Boolean inStock) {
        if (inStock == null) return;
        if (inStock) {
            criteriaList.add(Criteria.where(FIELD_QUANTITY).gt(0));
        } else {
            criteriaList.add(Criteria.where(FIELD_QUANTITY).lte(0));
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
        dto.setCategory(p.getCategory());
        dto.setQuantity(p.getQuantity());
        return dto;
    }
}
