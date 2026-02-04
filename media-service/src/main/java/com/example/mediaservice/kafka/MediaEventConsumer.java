package com.example.mediaservice.kafka;

import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import com.example.shared.kafka.KafkaTopics;
import com.example.shared.kafka.event.ProductDeletedEvent;
import com.example.shared.kafka.event.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Consumer for product and user events to cleanup orphan media files.
 * When a product or user is deleted, this service removes associated media.
 */
@Service
public class MediaEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(MediaEventConsumer.class);

    private final MediaRepository mediaRepository;
    private final Path storageLocation;

    public MediaEventConsumer(MediaRepository mediaRepository,
                              @org.springframework.beans.factory.annotation.Value("${media.storage.location:target/media}") String storagePath) {
        this.mediaRepository = mediaRepository;
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    /**
     * Handle product deletion events.
     * When a product is deleted, we must delete all associated media files.
     *
     * @param event the product deleted event
     */
    @KafkaListener(topics = KafkaTopics.PRODUCT_EVENTS, groupId = "media-service-group")
    public void handleProductEvent(Object event) {
        // Only handle ProductDeletedEvent
        if (event instanceof ProductDeletedEvent productDeletedEvent) {
            handleProductDeleted(productDeletedEvent);
        }
        // Ignore other product events (created, updated)
    }

    private void handleProductDeleted(ProductDeletedEvent event) {
        LOG.info("Received ProductDeletedEvent: {}", event);

        String productId = event.getProductId();

        // Find all media files for this product
        List<MediaFile> mediaFiles = mediaRepository.findByProductId(productId);

        if (mediaFiles.isEmpty()) {
            LOG.info("No media files found for product {}", productId);
            return;
        }

        LOG.info("Deleting {} media files for product {}", mediaFiles.size(), productId);

        for (MediaFile media : mediaFiles) {
            deleteMediaFile(media);
        }

        LOG.info("Finished cleaning up media for product {}", productId);
    }

    /**
     * Handle user deletion events.
     * When a user is deleted, we must delete all their media files.
     *
     * @param event the user deleted event
     */
    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "media-service-group")
    public void handleUserDeleted(UserDeletedEvent event) {
        LOG.info("Received UserDeletedEvent: {}", event);

        String userId = event.getUserId();

        // Find all media files owned by this user
        List<MediaFile> mediaFiles = mediaRepository.findByOwnerId(userId);

        if (mediaFiles.isEmpty()) {
            LOG.info("No media files found for user {}", userId);
            return;
        }

        LOG.info("Deleting {} media files for user {}", mediaFiles.size(), userId);

        for (MediaFile media : mediaFiles) {
            deleteMediaFile(media);
        }

        LOG.info("Finished cleaning up media for user {}", userId);
    }

    /**
     * Delete a media file from storage and database.
     *
     * @param media the media file to delete
     */
    private void deleteMediaFile(MediaFile media) {
        try {
            // Build file path: storageLocation/ownerId/filename
            String owner = media.getOwnerId() != null ? media.getOwnerId() : "public";
            Path filePath = storageLocation.resolve(owner).resolve(media.getFilename());

            // Delete physical file
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                LOG.info("Deleted file: {}", filePath);
            } else {
                LOG.warn("File not found for deletion: {}", filePath);
            }

            // Delete database record
            mediaRepository.deleteById(Objects.requireNonNull(media.getId()));
            LOG.info("Deleted media record: {}", media.getId());

        } catch (IOException e) {
            LOG.error("Failed to delete media file {}: {}", media.getId(), e.getMessage());
        }
    }
}
