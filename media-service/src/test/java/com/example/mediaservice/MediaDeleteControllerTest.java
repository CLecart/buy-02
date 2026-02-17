package com.example.mediaservice;

import com.example.mediaservice.model.MediaFile;
import com.example.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MediaDeleteControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MediaRepository mediaRepository;

    @AfterEach
    void cleanup() throws Exception {
        mediaRepository.deleteAll();
        Path dir = Path.of("target/media");
        if (Files.exists(dir)) {
            Files.walk(dir).sorted((a,b)->b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void delete_allowed_when_owner_matches() throws Exception {
        String owner = "del-owner";
        String filename = "del-file.png";
        MediaFile m = new MediaFile(owner, filename, "orig.png", "image/png", 10L, "cs", Instant.now());
        MediaFile saved = mediaRepository.save(m);

        Path file = Path.of("target/media").resolve(owner).resolve(filename);
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[]{1,2,3});

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(owner, null, java.util.List.of())
        );

        String id = java.util.Objects.requireNonNull(saved.getId());
        mvc.perform(delete("/api/media/" + id))
            .andExpect(status().isNoContent());
        assert !mediaRepository.findById(id).isPresent();
        assert !Files.exists(file);
    }

    @Test
    void delete_forbidden_when_not_owner() throws Exception {
        String owner = "owner-keep";
        String filename = "keep-file.png";
        MediaFile m = new MediaFile(owner, filename, "orig.png", "image/png", 10L, "cs", Instant.now());
        MediaFile saved = mediaRepository.save(m);

        Path file = Path.of("target/media").resolve(owner).resolve(filename);
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[]{1,2,3});

        // set auth as different user
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("other-user", null, java.util.List.of())
        );

        String id2 = java.util.Objects.requireNonNull(saved.getId());
        mvc.perform(delete("/api/media/" + id2))
            .andExpect(status().isForbidden());
        assert mediaRepository.findById(id2).isPresent();
        assert Files.exists(file);
    }
}
