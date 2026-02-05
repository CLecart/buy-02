package com.example.mediaservice.controller;

import com.example.mediaservice.dto.MediaUploadResponse;
import com.example.mediaservice.service.StorageService;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Controller exposing simple media endpoints: upload, serve and listing.
 *
 * <p>Listing supports pagination via request parameters `page`, `size` and `sort`.
 * The controller is defensive about null/empty inputs to satisfy null-safety
 * checks from static analysis and code auditors.
 */
@RestController
@RequestMapping("/api/media")
@Validated
public class MediaController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaController.class);

    private final StorageService storageService;
    private final Tika tika = new Tika();
    private final com.example.mediaservice.client.ProductClient productClient;
    private final com.example.mediaservice.repository.MediaRepository mediaRepository;

    /** Maximum allowed page size for listing endpoints (protects the service from large responses). */
    private static final int MAX_PAGE_SIZE = 200;
    private static final long MAX_UPLOAD_BYTES = 2L * 1024L * 1024L;
    private static final String PAGEABLE_REQUIRED = "pageable";

    public MediaController(StorageService storageService, com.example.mediaservice.client.ProductClient productClient,
                           com.example.mediaservice.repository.MediaRepository mediaRepository) {
        this.storageService = storageService;
        this.productClient = productClient;
        this.mediaRepository = mediaRepository;
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(HttpServletRequest request,
                                                      @RequestParam("file") MultipartFile file,
                                                      @RequestParam(name = "productId", required = false) String productId,
                                                      @RequestParam(name = "ownerId", required = false) String ownerId) {
        /* Quick preflight: if client supplied Content-Length header and it's larger than 2MB, reject early. */
        Long contentLength = parseContentLength(request.getHeader("Content-Length"));
        if (contentLength != null && contentLength > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("Uploaded file exceeds maximum allowed size of 2MB");
        }
        /* If the request is authenticated, prefer the authenticated principal as ownerId. */
        ownerId = resolveOwnerId(ownerId);

        /* If productId is provided, validate ownership via product-service (best-effort). */
        ResponseEntity<MediaUploadResponse> ownerCheck = validateProductOwnership(productId, ownerId);
        if (ownerCheck != null) return ownerCheck;

        // Store the file and get the media entity with ID
        com.example.mediaservice.model.MediaFile media = storageService.storeAndGetMedia(file, ownerId, productId);
        if (media == null) {
            return ResponseEntity.internalServerError().build();
        }

        String url = "/api/media/files/" + (media.getOwnerId() != null ? media.getOwnerId() : "public") + "/" + media.getFilename();
        MediaUploadResponse dto = new MediaUploadResponse(media.getId(), media.getFilename(), url, productId);

        // If productId is provided, link the media to the product (best-effort)
        if (productId != null && !productId.isBlank() && media.getId() != null) {
            String authHeader = request.getHeader("Authorization");
            String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
            productClient.addMediaToProduct(productId, media.getId(), token);
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/files/{ownerId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String ownerId, @PathVariable String filename) {
        Path path = storageService.load(ownerId, filename);
        try {
            UrlResource resource = new UrlResource(Objects.requireNonNull(path.toUri()));
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            MediaType mediaType = detectMediaType(path);
            return ResponseEntity.ok().contentType(Objects.requireNonNull(mediaType)).body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    /**
     * List media metadata with optional filtering and pagination.
     *
     * @param productId optional product id to filter by
     * @param ownerId optional owner id to filter by
     * @param page zero-based page index (default 0)
     * @param size page size (default 20)
     * @param sort sort expression in the form "property,asc|desc" (default "uploadedAt,desc")
     * @return paged response of media metadata
     */
    public ResponseEntity<com.example.mediaservice.dto.PagedResponse<com.example.mediaservice.dto.MediaMetadataDto>> list(
        @RequestParam(name = "productId", required = false) String productId,
        @RequestParam(name = "ownerId", required = false) String ownerId,
        @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
        @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
        @RequestParam(name = "sort", defaultValue = "uploadedAt,desc") String sort
    ) {
        // debug prints removed — use structured logging instead
        if (mediaRepository == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        log.debug("Listing media: page={}, size={}, sort={}", page, size, sort);

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            log.debug("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().build();
        }
        org.springframework.data.domain.Page<com.example.mediaservice.model.MediaFile> pageRes;
        if (productId != null && !productId.isBlank()) {
            pageRes = mediaRepository.findByProductId(
                productId,
                Objects.requireNonNull(buildPageable(page, size, sort), PAGEABLE_REQUIRED)
            );
        } else if (ownerId != null && !ownerId.isBlank()) {
            pageRes = mediaRepository.findByOwnerId(
                ownerId,
                Objects.requireNonNull(buildPageable(page, size, sort), PAGEABLE_REQUIRED)
            );
        } else {
            pageRes = mediaRepository.findAll(Objects.requireNonNull(buildPageable(page, size, sort), PAGEABLE_REQUIRED));
        }

        java.util.List<com.example.mediaservice.dto.MediaMetadataDto> dtos = pageRes.getContent().stream()
            .map(this::toMetadataDto)
            .toList();

        com.example.mediaservice.dto.PagedResponse<com.example.mediaservice.dto.MediaMetadataDto> resp = new com.example.mediaservice.dto.PagedResponse<>(
                dtos, pageRes.getTotalElements(), pageRes.getTotalPages(), pageRes.getNumber(), pageRes.getSize()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.example.mediaservice.dto.MediaMetadataDto> getById(@PathVariable("id") String id) {
        if (mediaRepository == null) return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
        String nonNullId = java.util.Objects.requireNonNull(id, "id");
        return mediaRepository.findById(nonNullId)
                .map(m -> new com.example.mediaservice.dto.MediaMetadataDto(
                m.getId(), m.getOwnerId(), m.getProductId(), m.getFilename(), m.getOriginalName(), m.getMimeType(), m.getSize(), m.getChecksum(), m.getUploadedAt(), m.getWidth(), m.getHeight()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) {
        if (mediaRepository == null) return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
        String nonNullId = java.util.Objects.requireNonNull(id, "id");
        var maybe = mediaRepository.findById(nonNullId);
        if (maybe.isEmpty()) return ResponseEntity.notFound().build();
        var m = maybe.get();

        String principal = resolvePrincipal();
        if (principal == null || !principal.equals(m.getOwnerId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        // attempt to delete file and metadata
        try {
            storageService.delete(m.getOwnerId(), m.getFilename());
        } catch (Exception ex) {
            log.warn("Failed to delete file for media id={}", id, ex);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        try {
            mediaRepository.deleteById(nonNullId);
        } catch (Exception ex) {
            log.warn("Failed to delete metadata for media id={}", id, ex);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.noContent().build();
    }

    private Long parseContentLength(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) return null;
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException ex) {
            log.debug("Invalid Content-Length header: {}", headerValue, ex);
            return null;
        }
    }

    private String resolveOwnerId(String currentOwnerId) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principal) {
            return principal;
        }
        return currentOwnerId;
    }

    private String resolvePrincipal() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principal) {
            return principal;
        }
        return null;
    }

    private org.springframework.data.domain.Pageable buildPageable(int page, int size, String sort) {
        org.springframework.data.domain.Sort.Order order = parseSortOrder(sort);
        return org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(order));
    }

    private ResponseEntity<MediaUploadResponse> validateProductOwnership(String productId, String ownerId) {
        if (productId == null || productId.isBlank()) return null;
        try {
            String productOwner = productClient.getOwnerId(productId);
            if (productOwner == null || !productOwner.equals(ownerId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_GATEWAY).build();
        }
        return null;
    }

    private MediaType detectMediaType(Path path) {
        String contentType = null;
        try {
            contentType = tika.detect(path);
        } catch (Exception ex) {
            log.debug("Tika detection failed for {}", path, ex);
        }
        if (contentType == null) {
            try {
                contentType = Files.probeContentType(path);
            } catch (IOException ex) {
                log.debug("Failed to probe content type for {}", path, ex);
            }
        }
        if (contentType != null) {
            try {
                return MediaType.parseMediaType(contentType);
            } catch (Exception ex) {
                log.debug("Invalid media type {}, falling back to octet-stream", contentType, ex);
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private org.springframework.data.domain.Sort.Order parseSortOrder(String sort) {
        org.springframework.data.domain.Sort.Order order = org.springframework.data.domain.Sort.Order.by("uploadedAt");
        String[] parts = (sort == null) ? new String[0] : sort.split(",");
        if (parts.length >= 1) {
            String prop = parts[0];
            if (prop != null && !prop.isBlank()) {
                org.springframework.data.domain.Sort.Direction dir = org.springframework.data.domain.Sort.Direction.DESC;
                if (parts.length >= 2 && "asc".equalsIgnoreCase(parts[1])) {
                    dir = org.springframework.data.domain.Sort.Direction.ASC;
                }
                order = new org.springframework.data.domain.Sort.Order(dir, java.util.Objects.requireNonNull(prop));
            }
        }
        return order;
    }

    private com.example.mediaservice.dto.MediaMetadataDto toMetadataDto(com.example.mediaservice.model.MediaFile m) {
        return new com.example.mediaservice.dto.MediaMetadataDto(
            m.getId(),
            m.getOwnerId(),
            m.getProductId(),
            m.getFilename(),
            m.getOriginalName(),
            m.getMimeType(),
            m.getSize(),
            m.getChecksum(),
            m.getUploadedAt(),
            m.getWidth(),
            m.getHeight()
        );
    }
}
