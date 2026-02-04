package com.example.shared.dto;

/**
 * DTO returned after successful authentication.
 */
public class AuthResponse {
    private String token;
    private long expiresInMs;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresInMs) {
        this.token = token;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
