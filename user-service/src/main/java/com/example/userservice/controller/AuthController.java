package com.example.userservice.controller;

import com.example.shared.dto.AuthRequest;
import com.example.shared.dto.AuthResponse;
import com.example.userservice.dto.SignupRequest;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller exposing signup and signin endpoints.
 *
 * <p>Public API for user authentication. Endpoints are intentionally small and return DTOs
 * so clients (web or mobile) can consume them easily. The controller delegates JWT
 * creation to the shared {@code JwtService} and never persists tokens.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.shared.security.JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, com.example.shared.security.JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Register a new user.
     *
     * @param req signup request (name, email, password, role)
     * @return 201 Created with an {@link com.example.shared.dto.AuthResponse} containing a JWT token
     */
    @PostMapping(value = "/signup", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        });

        String hashed = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getName(), req.getEmail(), hashed, req.getRole(), null);
        user = userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRole().name());

    String token = jwtService.generateToken(user.getId(), claims);
    return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, jwtService.getExpirationMs()));
    }

    /**
     * Authenticate existing user with email and password.
     *
     * @param req authentication request
     * @return 200 OK with an {@link com.example.shared.dto.AuthResponse} containing a JWT token
     */
    @PostMapping(value = "/signin", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody AuthRequest req) {
        var userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        var user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRole().name());
    String token = jwtService.generateToken(user.getId(), claims);
    return ResponseEntity.ok(new AuthResponse(token, jwtService.getExpirationMs()));
    }
}
