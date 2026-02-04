package com.example.shared.kafka.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when a user account is deleted.
 * Product-service and media-service consume this to cleanup user's products and media.
 */
public class UserDeletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String userId;
    private String userRole;
    private Instant timestamp;

    public UserDeletedEvent() {
        this.timestamp = Instant.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    public UserDeletedEvent(String userId, String userRole) {
        this();
        this.userId = userId;
        this.userRole = userRole;
    }

    // Getters and Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserDeletedEvent{"
                + "eventId='" + eventId + '\''
                + ", userId='" + userId + '\''
                + ", userRole='" + userRole + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}
