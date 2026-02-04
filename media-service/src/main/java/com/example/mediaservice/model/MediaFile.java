package com.example.mediaservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


/**
 * Media file metadata persisted in MongoDB when available.
 *
 * Relationship notes:
 * - Each Media can be associated to a Product via {@code productId} (references {@code product.id}).
 * - {@code ownerId} is the user who uploaded the file (should match product.ownerId when media is attached to a product).
 */
@Document(collection = "media_files")
public class MediaFile {

    @Id
    private String id;

    @Indexed
    private String ownerId;

    @Indexed
    /** Id of the Product this media is attached to (product.id). Optional if media is user avatar. */
    private String productId;

    private String filename;

    private String originalName;

    private String mimeType;

    private long size;

    private String checksum;

    private Instant uploadedAt;
    /** Optional image width (pixels) when available. */
    private Integer width;
    /** Optional image height (pixels) when available. */
    private Integer height;

    public MediaFile() {
    }

    public MediaFile(String ownerId, String filename, String originalName, String mimeType, long size, String checksum, Instant uploadedAt) {
        this.ownerId = ownerId;
        this.filename = filename;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.checksum = checksum;
        this.uploadedAt = uploadedAt;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
