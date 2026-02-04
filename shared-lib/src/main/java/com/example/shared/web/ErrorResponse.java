package com.example.shared.web;

import java.time.Instant;

/**
 * Standard API error response returned by controllers when an error occurs.
 */
public class ErrorResponse {
    private String error;
    private String message;

    /**
     * ISO-8601 formatted timestamp. Kept as String to avoid requiring jackson-datatype-jsr310
     * on minimal test setups.
     */
    private String timestamp;

    public ErrorResponse() {}

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
