package com.example.mediaservice.security;

import com.example.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Configuration
public class JwtConfig {

    @Value("${APP_JWT_SECRET:}")
    private String jwtSecret;

    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService() {
        return new JwtService(jwtSecret, 86_400_000L);
    }
}
