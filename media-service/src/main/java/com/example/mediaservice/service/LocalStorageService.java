package com.example.mediaservice.service;

import com.example.mediaservice.exception.FileStorageException;
import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path rootLocation;
    private final long maxBytes = 2L * 1024 * 1024;
    private final Tika tika = new Tika();
    private MediaRepository mediaRepository;

    public LocalStorageService(@Value("${media.storage.location:target/media}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directory", e);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setMediaRepository(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Override
    public Path store(MultipartFile file, String ownerId, String productId) {
        MediaFile media = storeAndGetMedia(file, ownerId, productId);
        if (media != null) {
            String owner = media.getOwnerId() == null ? "public" : media.getOwnerId();
            return Paths.get(owner, media.getFilename());
        }
        return Paths.get("public", "unknown");
    }

    @Override
    public MediaFile storeAndGetMedia(MultipartFile file, String ownerId, String productId) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 2MB");
        }

        String originalRaw = file.getOriginalFilename();
        String original = StringUtils.cleanPath(originalRaw == null ? "" : originalRaw);
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) ext = original.substring(idx + 1).toLowerCase();

        if (!isAllowedExtension(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: " + ext);
        }

        String detectedMime;
        try {
            detectedMime = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to detect file type");
        }

        if (detectedMime == null || !detectedMime.startsWith("image/")) {
            throw new IllegalArgumentException("File content type mismatch or unsupported: " + detectedMime);
        }

        String filename = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
        Path targetDir = rootLocation.resolve(ownerId == null ? "public" : ownerId);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            String checksum = null;
            try (var in = Files.newInputStream(target)) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    md.update(buf, 0, r);
                }
                checksum = java.util.HexFormat.of().formatHex(md.digest());
            } catch (Exception e) {
                log.debug("Failed to compute checksum", e);
            }

            if (mediaRepository != null) {
                String mimeType = null;
                try {
                    mimeType = tika.detect(target);
                } catch (Exception e) {
                    log.debug("Failed to detect MIME type", e);
                }
                MediaFile meta = new MediaFile(ownerId, filename, original, mimeType, file.getSize(), checksum, java.time.Instant.now());
                try {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(target.toFile());
                    if (img != null) {
                        meta.setWidth(img.getWidth());
                        meta.setHeight(img.getHeight());
                    }
                } catch (Exception e) {
                    log.debug("Failed to read image dimensions", e);
                }
                if (productId != null && !productId.isBlank()) {
                    meta.setProductId(productId);
                }
                try {
                    meta = mediaRepository.save(meta);
                    return meta;
                } catch (Exception ex) {
                    log.warn("Failed to save media metadata", ex);
                }
            }

            MediaFile transientMeta = new MediaFile(ownerId, filename, original, detectedMime, file.getSize(), checksum, java.time.Instant.now());
            if (productId != null && !productId.isBlank()) {
                transientMeta.setProductId(productId);
            }
            return transientMeta;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    private boolean isAllowedExtension(String ext) {
        if (ext == null || ext.isEmpty()) return false;
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "gif".equals(ext);
    }

    @Override
    public Path load(String ownerId, String filename) {
        Path target = rootLocation.resolve(ownerId == null ? "public" : ownerId).resolve(filename).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new FileStorageException("Invalid path");
        }
        if (!Files.exists(target) || !Files.isReadable(target)) {
            throw new FileStorageException("File not found: " + filename);
        }
        return target;
    }

    @Override
    public boolean delete(String ownerId, String filename) {
        Path target = rootLocation.resolve(ownerId == null ? "public" : ownerId).resolve(filename).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new FileStorageException("Invalid path");
        }
        try {
            return Files.deleteIfExists(target);
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }
}
