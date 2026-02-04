package com.example.productservice.security;

import com.example.shared.security.JwtAuthenticationFilter;
import com.example.shared.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String PRODUCTS_PATH = "/api/products/**";
    private static final String SELLER_ROLE = "SELLER";

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.GET, PRODUCTS_PATH).permitAll()
                // Only SELLER role can create, update, delete products
            .requestMatchers(HttpMethod.POST, PRODUCTS_PATH).hasRole(SELLER_ROLE)
            .requestMatchers(HttpMethod.PUT, PRODUCTS_PATH).hasRole(SELLER_ROLE)
            .requestMatchers(HttpMethod.DELETE, PRODUCTS_PATH).hasRole(SELLER_ROLE)
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
