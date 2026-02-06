package com.example.mediaservice;

import com.example.mediaservice.client.ProductClient;
import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.MongoDBContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MediaMetadataPersistenceIntegrationTest {

    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.12");

    static {
        mongo.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        String testJwtSecret = java.util.HexFormat.of().formatHex(secretBytes);
        reg.add("APP_JWT_SECRET", () -> testJwtSecret);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    MediaRepository mediaRepository;

    @MockBean
    ProductClient productClient;

    @AfterEach
    void cleanup() {
        mediaRepository.deleteAll();
    }

    @Test
    void upload_persists_metadata_with_checksum_and_mime() throws Exception {
        String productId = "prod-persist-1";
        String ownerId = "owner-persist-1";
        when(productClient.getOwnerId(productId)).thenReturn(ownerId);

        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "persist.png", MediaType.IMAGE_PNG_VALUE, png);

        mvc.perform(multipart("/api/media/upload")
            .file(file)
            .param("productId", productId)
            .param("ownerId", ownerId)
            .with(java.util.Objects.requireNonNull(user(ownerId).roles("SELLER"))))
            .andExpect(status().isOk());

        List<MediaFile> list = mediaRepository.findByProductId(productId);
        assertThat(list).hasSize(1);
        MediaFile meta = list.get(0);
        assertThat(meta.getOwnerId()).isEqualTo(ownerId);
        assertThat(meta)
          .satisfies(m -> {
            assertThat(m.getProductId()).isEqualTo(productId);
            assertThat(m.getSize()).isEqualTo(png.length);
            assertThat(m.getChecksum()).isNotNull();
            assertThat(m.getMimeType()).startsWith("image/");
            assertThat(m.getWidth()).isEqualTo(1);
            assertThat(m.getHeight()).isEqualTo(1);
          });
    }
}
