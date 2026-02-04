package com.example.mediaservice;

import com.example.mediaservice.client.ProductClient;
import org.junit.jupiter.api.Test;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MediaUploadOwnershipIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ProductClient productClient;

    @Test
    void upload_allowed_when_owner_matches() throws Exception {
        String productId = "prod-1";
        String ownerId = "owner-1";
        when(productClient.getOwnerId(productId)).thenReturn(ownerId);

        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE, png);

        mvc.perform(multipart("/api/media/upload").file(file).param("productId", productId).param("ownerId", ownerId).with(Objects.requireNonNull(csrf())))
                .andExpect(status().isOk());
    }

    @Test
    void upload_forbidden_when_owner_mismatch() throws Exception {
        String productId = "prod-1";
        String ownerId = "owner-1";
        when(productClient.getOwnerId(productId)).thenReturn("other-owner");

        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE, png);

        mvc.perform(multipart("/api/media/upload").file(file).param("productId", productId).param("ownerId", ownerId).with(Objects.requireNonNull(csrf())))
                .andExpect(status().isForbidden());
    }
}
