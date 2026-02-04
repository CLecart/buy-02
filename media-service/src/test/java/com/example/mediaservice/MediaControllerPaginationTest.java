package com.example.mediaservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MediaControllerPaginationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void list_withNegativePage_returnsBadRequest() throws Exception {
        mvc.perform(get("/api/media").param("page", "-1").param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_argument"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void list_withZeroSize_returnsBadRequest() throws Exception {
        mvc.perform(get("/api/media").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_argument"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void list_withTooLargeSize_returnsBadRequest() throws Exception {
        mvc.perform(get("/api/media").param("page", "0").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_argument"))
                .andExpect(jsonPath("$.message").exists());
    }

    
}
