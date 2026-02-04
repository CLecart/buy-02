package com.example.mediaservice.config;

import com.example.shared.security.JwtService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }
}
