package com.example.productservice.security;

import com.example.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${APP_JWT_SECRET:}")
    private String jwtSecret;

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, 86_400_000L);
    }
}
