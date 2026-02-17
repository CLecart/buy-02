package com.example.mediaservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.Mockito;

class LocalStorageServiceTest {

    private final Path testRoot = Paths.get("target/test-media").toAbsolutePath().normalize();

    @AfterEach
    void cleanup() {
        try {
            if (Files.exists(testRoot)) {
                Files.walk(testRoot).sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
            }
        } catch (IOException e) {
            // Ignoré : nettoyage best-effort
        }
    }

    @Test
    void store_tooLarge_throwsIllegalArgumentException() {
        int size = 2 * 1024 * 1024 + 10;
        byte[] big = new byte[size];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", big);

        LocalStorageService svc = new LocalStorageService(testRoot.toString());

        assertThrows(IllegalArgumentException.class, () -> svc.store(file, null, null));
    }

    @Test
    void store_mimeMismatch_throwsIllegalArgumentException() {
        byte[] txt = "this is plain text".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.jpg", "text/plain", txt);

        LocalStorageService svc = new LocalStorageService(testRoot.toString());

        assertThrows(IllegalArgumentException.class, () -> svc.store(file, null, null));
    }

    @Test
    void store_smallPng_succeedsAndFileExists() {
        String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
        byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
        MockMultipartFile file = new MockMultipartFile("file", "ok.png", "image/png", pngBytes);

        LocalStorageService svc = new LocalStorageService(testRoot.toString());

        Path rel = svc.store(file, null, null);
        Path absolute = testRoot.resolve(rel).normalize();
        try {
            assertThat(Files.exists(absolute)).isTrue();
            assertThat(Files.size(absolute)).isEqualTo(pngBytes.length);
        } catch (IOException e) {
            org.junit.jupiter.api.Assertions.fail("IOException should not occur: " + e.getMessage());
        }
    }

    @Test
    void store_withRepository_savesMetadataWithDimensions() {
        String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
        byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
        MockMultipartFile file = new MockMultipartFile("file", "ok.png", "image/png", pngBytes);

        LocalStorageService svc = new LocalStorageService(testRoot.toString());

        com.example.mediaservice.repository.MediaRepository repo = Mockito.mock(com.example.mediaservice.repository.MediaRepository.class);
        svc.setMediaRepository(repo);

        Path rel = svc.store(file, "tester", null);
        assertThat(rel).isNotNull();

        var invs = Mockito.mockingDetails(repo).getInvocations();
        org.assertj.core.api.Assertions.assertThat(invs).isNotEmpty();
        var saveInv = invs.stream().filter(i -> "save".equals(i.getMethod().getName())).findFirst().orElseThrow();
        Object arg0 = saveInv.getArgument(0);
        com.example.mediaservice.model.MediaFile meta = java.util.Objects.requireNonNull((com.example.mediaservice.model.MediaFile) arg0);
        assertThat(meta).isNotNull();
        assertThat(meta.getWidth()).isNotNull();
        assertThat(meta.getHeight()).isNotNull();
        assertThat(meta.getWidth()).isGreaterThan(0);
        assertThat(meta.getHeight()).isGreaterThan(0);
    }

}
