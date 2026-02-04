package com.example.userservice.controller;

import com.example.shared.dto.UserDto;
import com.example.userservice.kafka.UserEventProducer;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Controller exposant des endpoints utilisateur protégés.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L; // 2MB
    private static final Path AVATAR_STORAGE_PATH = Paths.get("uploads", "avatars");
    private static final String ERROR_KEY = "error";

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserController(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }

    /**
     * Retourne les informations publiques de l'utilisateur authentifié.
     * Le principal contient l'ID utilisateur (subject) tel que produit par JwtAuthenticationFilter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        String principal;
        Object p = auth.getPrincipal();
        if (p instanceof String principalStr) {
            principal = principalStr;
        } else if (auth.getName() != null) {
            principal = auth.getName();
        } else {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(java.util.Objects.requireNonNull(principal));
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User u = userOpt.get();
        UserDto dto = new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getAvatarUrl());
        return ResponseEntity.ok(dto);
    }

    /**
     * Upload or update the avatar for the authenticated SELLER.
     * Accepts image files (JPEG, PNG, GIF) up to 2MB.
     *
     * @param file the avatar image file
     * @return the updated user DTO with the new avatar URL
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Object> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (!(principal instanceof String userId)) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(java.util.Objects.requireNonNull(userId, "userId"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();

        // Validate file size
        if (file.getSize() > MAX_AVATAR_SIZE) {
            return ResponseEntity.badRequest().body(java.util.Map.of(ERROR_KEY, "File size exceeds 2MB limit"));
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(java.util.Map.of(ERROR_KEY, "Only JPEG, PNG, and GIF images are allowed"));
        }

        try {
            // Ensure storage directory exists
            Files.createDirectories(AVATAR_STORAGE_PATH);

            // Generate unique filename
            String extension = getFileExtension(file.getOriginalFilename());
            String filename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path targetPath = AVATAR_STORAGE_PATH.resolve(filename);

            // Delete old avatar if exists
            String oldAvatarUrl = user.getAvatarUrl();
            deleteAvatarFileIfExists(oldAvatarUrl);

            // Save new avatar
            file.transferTo(java.util.Objects.requireNonNull(targetPath.toFile(), "targetPath"));

            // Update user with new avatar URL
            String avatarUrl = "/api/users/avatars/" + filename;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getAvatarUrl());
            return ResponseEntity.ok(dto);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of(ERROR_KEY, "Failed to save avatar"));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * Serve avatar files publicly.
     *
     * @param filename the avatar filename
     * @return the avatar file as a resource
     */
    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> serveAvatar(
            @org.springframework.web.bind.annotation.PathVariable String filename) {
        try {
            Path filePath = AVATAR_STORAGE_PATH.resolve(java.util.Objects.requireNonNull(filename, "filename"));
            java.net.URI fileUri = java.util.Objects.requireNonNull(filePath.toUri(), "fileUri");
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(fileUri);

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete the authenticated user's account.
     * This publishes a UserDeletedEvent to Kafka for cascade deletion
     * of the user's products and media files.
     *
     * @return 204 No Content on success
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (!(principal instanceof String userId)) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(java.util.Objects.requireNonNull(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Delete avatar file if exists
        String avatarUrl = user.getAvatarUrl();
        deleteAvatarFileIfExists(avatarUrl);

        // Publish event for cascade deletion (products and media)
        userEventProducer.publishUserDeleted(userId, user.getRole().name());

        // Delete user from database
        userRepository.deleteById(userId);

        return ResponseEntity.noContent().build();
    }

    private void deleteAvatarFileIfExists(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }

        try {
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path avatarPath = AVATAR_STORAGE_PATH.resolve(filename);
            Files.deleteIfExists(avatarPath);
        } catch (Exception ignored) {
            // Ignore deletion errors
        }
    }
}
