package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByOwnerId(String ownerId);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
