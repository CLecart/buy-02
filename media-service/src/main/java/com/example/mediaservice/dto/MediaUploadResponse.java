package com.example.mediaservice.dto;

public class MediaUploadResponse {
    private String id;
    private String filename;
    private String url;
    private String productId;

    public MediaUploadResponse() {}

    public MediaUploadResponse(String filename, String url) {
        this.filename = filename;
        this.url = url;
    }

    public MediaUploadResponse(String id, String filename, String url, String productId) {
        this.id = id;
        this.filename = filename;
        this.url = url;
        this.productId = productId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
