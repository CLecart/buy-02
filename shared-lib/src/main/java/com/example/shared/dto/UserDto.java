package com.example.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Public representation of a user for API responses and token claims.
 */
public class UserDto {
    private String id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private String avatarUrl;

    private List<String> roles;

    /** Single role field for backward compatibility with user-service. */
    private String role;

    public UserDto() {
    }

    public UserDto(String id, String name, String email, String avatarUrl, List<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.roles = roles;
        this.role = (roles != null && !roles.isEmpty()) ? roles.get(0) : null;
    }

    /**
     * Constructor compatible with user-service Role enum.
     */
    public UserDto(String id, String name, String email, Object role, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role.toString() : null;
        this.roles = this.role != null ? List.of(this.role) : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
