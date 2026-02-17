package com.example.mediaservice;

import com.example.mediaservice.client.ProductClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerOwnershipTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ProductClient productClient;

    @AfterEach
    void cleanup() throws Exception {
        Path dir = Path.of("target/media");
        if (Files.exists(dir)) {
            Files.walk(dir).sorted((a,b)->b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void upload_forOwner_allowed() throws Exception {
        when(productClient.getOwnerId("prod-1")).thenReturn("owner-1");

        byte[] pngBytes = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file","test.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

        
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner-1", null, java.util.List.of())
        );

    mvc.perform(multipart("/api/media/upload").file(file).param("productId", "prod-1").with(java.util.Objects.requireNonNull(csrf())))
        .andExpect(status().isOk());
    }

    @Test
    void upload_forNonOwner_forbidden() throws Exception {
        when(productClient.getOwnerId("prod-1")).thenReturn("someone-else");

        byte[] pngBytes = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file","test.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

        
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner-1", null, java.util.List.of())
        );

    mvc.perform(multipart("/api/media/upload").file(file).param("productId", "prod-1").with(java.util.Objects.requireNonNull(csrf())))
        .andExpect(status().isForbidden());
    }
}
