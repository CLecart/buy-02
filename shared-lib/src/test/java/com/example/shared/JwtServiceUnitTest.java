package com.example.shared;

import com.example.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JwtServiceUnitTest {

    

    @AfterEach
    void tearDown() {
        
    }

    @Test
    void createAndParseToken_shouldReturnClaimsAndSubject() {
    JwtService svc = TestJwtUtil.createJwtService(3_600_000L);
        String token = svc.generateToken("alice", Map.of("roles", "USER,ADMIN", "org", "acme"));
        Assertions.assertNotNull(token, "token should be produced");

        Claims claims = svc.parseToken(token);
        Assertions.assertEquals("alice", claims.getSubject());
        Assertions.assertEquals("acme", claims.get("org"));
        Assertions.assertEquals("USER,ADMIN", claims.get("roles"));
    }

    @Test
    void invalidToken_shouldThrow() {
    JwtService svc = TestJwtUtil.createJwtService(3_600_000L);
        Assertions.assertThrows(JwtException.class, () -> svc.parseToken("not-a-token"));
    }

    @Test
    void getExpirationMs_returnsConfiguredValue() {
        long ttl = 10_000L;
    JwtService svc = new JwtService(TestJwtUtil.generateSecret(), ttl);
        Assertions.assertEquals(ttl, svc.getExpirationMs());
    }

    @Test
    void shortSecret_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new JwtService("shortsecret", 1000L));
    }
}
