package com.example.shared;

import com.example.shared.security.JwtService;

/**
 * Test utilities for JWT-related tests.
 */
public final class TestJwtUtil {
    private TestJwtUtil() {}

    public static String generateSecret() {
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        return java.util.HexFormat.of().formatHex(secretBytes);
    }

    public static JwtService createJwtService(long expirationMs) {
        return new JwtService(generateSecret(), expirationMs);
    }

    public static JwtService createJwtService() {
        return createJwtService(3_600_000L);
    }
}
