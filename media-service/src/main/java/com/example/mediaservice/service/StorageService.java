package com.example.mediaservice.service;

import com.example.mediaservice.model.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {
    /**
     * Store the provided multipart file and return the relative path where it was stored.
    *
    * @param file the uploaded file
    * @param ownerId the id of the uploading user (may be null for public)
    * @param productId optional product id this media is attached to
    */
    Path store(MultipartFile file, String ownerId, String productId);

    /**
     * Store the provided multipart file and return the saved MediaFile entity.
     * Returns null if metadata persistence is not available.
     */
    default MediaFile storeAndGetMedia(MultipartFile file, String ownerId, String productId) {
        store(file, ownerId, productId);
        return null;
    }

    /**
     * Load a stored file as a Path for the given owner and filename.
     * The implementation should ensure path normalization and prevent path traversal.
     */
    Path load(String ownerId, String filename);

    /**
     * Delete a stored file for the given owner and filename. Implementations should
     * ensure safe path normalization and return true if the file was deleted.
     */
    boolean delete(String ownerId, String filename);
}
