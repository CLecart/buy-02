package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the user-service Spring Boot application.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.userservice", "com.example.shared"})
@EnableMongoRepositories(basePackages = {"com.example.userservice.repository", "com.example.shared.repository"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
