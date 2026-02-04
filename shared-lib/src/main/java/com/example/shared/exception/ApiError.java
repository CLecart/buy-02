package com.example.shared.exception;

/**
 * Simple API error representation used by services to standardize error responses.
 */
public final class ApiError {
    private final String message;
    private final String code;

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
