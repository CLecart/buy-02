package com.example.userservice.config;

import com.example.shared.security.JwtAuthenticationFilter;
import com.example.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration to create JwtService and related beans from environment.
 */
@Configuration
public class JwtConfig {

    @Value("${APP_JWT_SECRET:}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @Value("${BCRYPT_STRENGTH:12}")
    private int bcryptStrength;

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, expirationMs);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
