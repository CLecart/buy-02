package com.example.mediaservice.dto;

import java.time.Instant;

/**
 * DTO representing persisted media metadata returned by media listing endpoints.
 *
 * <p>Fields are informational and safe to expose to authenticated clients. The {@code ownerId}
 * indicates who uploaded the media and {@code productId} links to the owning product.
 */
public class MediaMetadataDto {
    private String id;
    private String ownerId;
    private String productId;
    private String filename;
    private String originalName;
    private String mimeType;
    private long size;
    private String checksum;
    private Instant uploadedAt;
    private Integer width;
    private Integer height;

    public MediaMetadataDto() {}

    public MediaMetadataDto(String id, String ownerId, String productId, String filename, String originalName, String mimeType, long size, String checksum, Instant uploadedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.productId = productId;
        this.filename = filename;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.checksum = checksum;
        this.uploadedAt = uploadedAt;
    }

    public MediaMetadataDto(String id, String ownerId, String productId, String filename, String originalName, String mimeType, long size, String checksum, Instant uploadedAt, Integer width, Integer height) {
        this(id, ownerId, productId, filename, originalName, mimeType, size, checksum, uploadedAt);
        this.width = width;
        this.height = height;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
}
