package com.example.mediaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.mediaservice", "com.example.shared"})
@EnableMongoRepositories(basePackages = {"com.example.mediaservice.repository", "com.example.shared.repository"})
public class MediaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
