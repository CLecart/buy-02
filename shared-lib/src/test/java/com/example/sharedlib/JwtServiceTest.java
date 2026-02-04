package com.example.sharedlib;

import com.example.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Compatibility test kept for older expectations — uses canonical JwtService API.
 */
public class JwtServiceTest {

    @Test
    void createAndParseToken_usingCanonicalApi() {
    JwtService svc = com.example.shared.TestJwtUtil.createJwtService(3_600_000L);
        String token = Assertions.assertDoesNotThrow(() -> svc.generateToken("user-1", Map.of("role", "SELLER")));
        Assertions.assertNotNull(token);

        Claims parsed = Assertions.assertDoesNotThrow(() -> svc.parseToken(token));
        Assertions.assertEquals("user-1", parsed.getSubject());
        Assertions.assertEquals("SELLER", parsed.get("role"));
    }

    @Test
    void secretTooShortShouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 3600L));
    }
}
