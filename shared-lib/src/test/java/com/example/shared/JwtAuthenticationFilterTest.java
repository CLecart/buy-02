package com.example.shared;

import com.example.shared.security.JwtAuthenticationFilter;
import com.example.shared.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

class JwtAuthenticationFilterTest {


    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void filter_shouldPopulateSecurityContext_whenTokenValid() throws Exception {
    JwtService svc = TestJwtUtil.createJwtService(3_600_000L);
        String token = svc.generateToken("bob", Map.of("roles", "SELLER"));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(svc);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, resp, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertNotNull(auth, "authentication should be set");
        Assertions.assertEquals("bob", auth.getPrincipal());
        Assertions.assertFalse(auth.getAuthorities().isEmpty());
    }

    @Test
    void filter_shouldNotPopulateSecurityContext_whenTokenInvalid() throws Exception {
    JwtService svc = TestJwtUtil.createJwtService(3_600_000L);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(svc);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + "bad.token.here");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, resp, chain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertNull(auth, "authentication should not be set for invalid token");
    }
}
