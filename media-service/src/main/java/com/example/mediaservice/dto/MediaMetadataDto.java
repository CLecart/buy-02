package com.example.mediaservice.dto;

import java.time.Instant;

/**
 * DTO representing persisted media metadata returned by media listing endpoints.
 * <p>
 * Fields are informational and safe to expose to authenticated clients. The {@code ownerId}
 * indicates who uploaded the media and {@code productId} links to the owning product.
 *
 * <p>Use the {@link Builder} to construct instances for better readability and maintainability.
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

    /**
     * Default constructor.
     */
    public MediaMetadataDto() {}

    private MediaMetadataDto(Builder builder) {
        this.id = builder.id;
        this.ownerId = builder.ownerId;
        this.productId = builder.productId;
        this.filename = builder.filename;
        this.originalName = builder.originalName;
        this.mimeType = builder.mimeType;
        this.size = builder.size;
        this.checksum = builder.checksum;
        this.uploadedAt = builder.uploadedAt;
        this.width = builder.width;
        this.height = builder.height;
    }


    /**
     * Builder for {@link MediaMetadataDto}.
     */
    public static class Builder {
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

        public Builder id(String id) { this.id = id; return this; }
        public Builder ownerId(String ownerId) { this.ownerId = ownerId; return this; }
        public Builder productId(String productId) { this.productId = productId; return this; }
        public Builder filename(String filename) { this.filename = filename; return this; }
        public Builder originalName(String originalName) { this.originalName = originalName; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder checksum(String checksum) { this.checksum = checksum; return this; }
        public Builder uploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; return this; }
        public Builder width(Integer width) { this.width = width; return this; }
        public Builder height(Integer height) { this.height = height; return this; }
        public MediaMetadataDto build() { return new MediaMetadataDto(this); }
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
