package com.example.mediaservice;

import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MediaMetadataControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MediaRepository mediaRepository;

    @Test
    void getById_returns_metadata() throws Exception {
        MediaFile m = new MediaFile("owner-x", "file-x.png", "orig.png", "image/png", 123L, "deadbeef", Instant.now());
        m.setProductId("prod-x");
        m.setWidth(1);
        m.setHeight(1);
        var saved = mediaRepository.save(m);

        String id = java.util.Objects.requireNonNull(saved.getId());

        mvc.perform(get("/api/media/" + id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.ownerId").value("owner-x"))
            .andExpect(jsonPath("$.productId").value("prod-x"))
            .andExpect(jsonPath("$.mimeType").value("image/png"))
            .andExpect(jsonPath("$.width").isNumber())
            .andExpect(jsonPath("$.height").isNumber());
    }
}
