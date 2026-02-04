package com.example.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter that extracts JWT from Authorization header, validates it via {@link JwtService}
 * and populates the SecurityContext with a UsernamePasswordAuthenticationToken.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                    @org.springframework.lang.NonNull HttpServletResponse response,
                    @org.springframework.lang.NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var claims = jwtService.parseToken(token);
                String subject = claims.getSubject();
                Object rolesObj = claims.get("roles");
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (rolesObj instanceof String) {
                    Arrays.stream(((String) rolesObj).split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(r -> {
                                // Add role with ROLE_ prefix for hasRole() and without for hasAuthority()
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                                authorities.add(new SimpleGrantedAuthority(r));
                            });
                }

                var authToken = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ex) {
            }
        }

        filterChain.doFilter(request, response);
    }
}
