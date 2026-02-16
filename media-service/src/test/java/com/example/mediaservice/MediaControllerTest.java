package com.example.mediaservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.util.Objects;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    MockMvc mvc;

    @AfterEach
    void cleanup() throws Exception {
        Path dir = Path.of("target/media");
        if (Files.exists(dir)) {
            Files.walk(dir).sorted((a,b)->b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void upload_acceptsPng() throws Exception {
    String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
    byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
    MockMultipartFile file = new MockMultipartFile("file","test.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

    RequestPostProcessor csrfProcessor = Objects.requireNonNull(csrf());
    mvc.perform(multipart("/api/media/upload").file(file).with(csrfProcessor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.filename").isNotEmpty())
        .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void upload_rejectsTooLargeFile() throws Exception {
        int size = 2 * 1024 * 1024 + 10;
        byte[] big = new byte[size];
        MockMultipartFile file = new MockMultipartFile("file","big.png", MediaType.IMAGE_PNG_VALUE, big);
        RequestPostProcessor csrfProcessor = Objects.requireNonNull(csrf());
        mvc.perform(multipart("/api/media/upload").file(file).with(csrfProcessor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_argument"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void upload_rejectsMimeMismatch() throws Exception {
        byte[] txt = "this is not an image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file","fake.jpg", MediaType.TEXT_PLAIN_VALUE, txt);
        RequestPostProcessor csrfProcessor = Objects.requireNonNull(csrf());
        mvc.perform(multipart("/api/media/upload").file(file).with(csrfProcessor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_argument"))
                .andExpect(jsonPath("$.message").exists());
    }
}
