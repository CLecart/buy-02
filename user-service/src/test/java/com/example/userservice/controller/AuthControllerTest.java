package com.example.userservice.controller;

import com.example.shared.dto.AuthRequest;
 
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.example.shared.security.JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void signin_invalidCredentials_returnsUnauthorized() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("noone@example.com");
        req.setPassword("wrong");

        when(userRepository.findByEmail("noone@example.com")).thenReturn(Optional.empty());

    mockMvc.perform(post("/api/auth/signin")
            .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
            .content("{\"email\":\"noone@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
