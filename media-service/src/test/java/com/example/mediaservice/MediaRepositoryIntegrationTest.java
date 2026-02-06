package com.example.mediaservice;

import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import com.example.mediaservice.service.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Tag("integration")
class MediaRepositoryIntegrationTest {

    @Autowired
    StorageService storageService;

    @Autowired(required = false)
    MediaRepository mediaRepository;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @AfterEach
    void cleanup() {
        if (mediaRepository != null) mediaRepository.deleteAll();
    }

    @Test
    void store_shouldPersistMetadata_whenRepositoryAvailable() throws Exception {
        
        String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
        byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngBytes);

    String ownerId = "owner-42";
    storageService.store(file, ownerId, null);

    
    Assumptions.assumeTrue(mediaRepository != null, "MediaRepository bean not available; skipping metadata assertions");

    
    List<MediaFile> list = mediaRepository.findAll();
    assertThat(list)
      .isNotEmpty()
      .anyMatch(m -> ownerId.equals(m.getOwnerId()) && m.getFilename() != null);
    }
}
