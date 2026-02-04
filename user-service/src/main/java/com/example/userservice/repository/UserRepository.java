package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for User entities.
 */
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
