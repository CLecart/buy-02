package com.example.shared.exception;

/**
 * Custom exception for Kafka event processing errors.
 * Used to distinguish event handling failures from generic runtime exceptions.
 */
public class EventProcessingException extends RuntimeException {

    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
