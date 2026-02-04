package com.example.mediaservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProductClient(RestTemplate restTemplate, @Value("${product.service.base-url:http://localhost:8082}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieve the ownerId for a given product id by calling product-service GET /api/products/{id}
     * Returns null if product exists but no owner present.
     * Throws RestClientException on transport errors or HttpClientErrorException.NotFound if not found.
     */
    public String getOwnerId(String productId) {
        String url = baseUrl + "/api/products/" + productId;
        Map<?,?> dto = restTemplate.getForObject(url, Map.class);
        if (dto == null) return null;
        Object owner = dto.get("ownerId");
        return owner == null ? null : owner.toString();
    }

    /**
     * Add a media ID to a product by calling product-service POST /api/products/{productId}/media/{mediaId}
     * Returns true on success, false otherwise.
     */
    public boolean addMediaToProduct(String productId, String mediaId, String authToken) {
        String url = baseUrl + "/api/products/" + productId + "/media/" + mediaId;
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (authToken != null && !authToken.isBlank()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            restTemplate.postForEntity(url, entity, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
