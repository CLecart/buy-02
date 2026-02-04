package com.example.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Service for generating and validating JWT tokens.
 * <p>
 * This implementation is lightweight and intended to be used by microservices.
 */
public final class JwtService {

    private final Key signingKey;
    private final long expirationMs;

    /**
     * Create a JwtService instance.
     *
     * @param secret       HMAC secret (must be strong); not committed to VCS; provided from environment.
     * @param expirationMs token expiration in milliseconds
     */
    public JwtService(String secret, long expirationMs) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("JWT secret must be set and at least 16 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a signed JWT token for the given subject and claims.
     *
     * @param subject subject (typically user id or email)
     * @param claims  additional claims to include
     * @return compact JWT string
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expirationMs);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(issuedAt)
        .setExpiration(expiresAt)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
    }

    /**
     * Parse and validate the token returning claims.
     *
     * @param token compact JWT string
     * @return parsed claims
     * @throws io.jsonwebtoken.JwtException if token invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Return configured token expiration (milliseconds).
     * Useful for controllers to expose expiry information to clients.
     */
    public long getExpirationMs() {
        return expirationMs;
    }
}
