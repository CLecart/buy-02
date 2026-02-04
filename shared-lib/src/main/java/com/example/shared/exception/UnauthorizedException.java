package com.example.shared.exception;

/**
 * Exception thrown when an operation is not authorized.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
