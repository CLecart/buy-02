package com.example.mediaservice;

import com.example.mediaservice.client.ProductClient;
import org.junit.jupiter.api.Test;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"APP_JWT_SECRET=0123456789abcdef0123456789abcdef"})
@SuppressWarnings("null")
public class MediaUploadE2ETest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ProductClient productClient;

    @Test
    void upload_allowed_when_product_owner_matches() throws Exception {
        String productId = "prod-123";
        String ownerId = "owner-abc";

        MockRestServiceServer server = MockRestServiceServer.bindTo(Objects.requireNonNull(restTemplate)).ignoreExpectOrder(true).build();
        server.expect(once(), requestTo("http://localhost:8082/api/products/" + productId))
                .andExpect(method(Objects.requireNonNull(GET)))
                .andRespond(withSuccess("{\"id\":\"prod-123\", \"ownerId\": \"owner-abc\"}", MediaType.APPLICATION_JSON));
        server.expect(manyTimes(), requestTo(startsWith("http://localhost:8082/api/products/prod-123/media/")))
                .andExpect(method(Objects.requireNonNull(POST)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "ok.png", MediaType.IMAGE_PNG_VALUE, png);

        mvc.perform(multipart("/api/media/upload").file(file).param("productId", productId).param("ownerId", ownerId).with(Objects.requireNonNull(csrf())))
                .andExpect(status().isOk());
    }

    @Test
    void upload_forbidden_when_product_owner_different() throws Exception {
        String productId = "prod-123";
        String ownerId = "owner-abc";

        MockRestServiceServer server = MockRestServiceServer.createServer(Objects.requireNonNull(restTemplate));
        server.expect(once(), requestTo("http://localhost:8082/api/products/" + productId))
                .andExpect(method(Objects.requireNonNull(GET)))
                .andRespond(withSuccess("{\"id\":\"prod-123\", \"ownerId\": \"other-owner\"}", MediaType.APPLICATION_JSON));

        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "bad.png", MediaType.IMAGE_PNG_VALUE, png);

        mvc.perform(multipart("/api/media/upload").file(file).param("productId", productId).param("ownerId", ownerId).with(Objects.requireNonNull(csrf())))
                .andExpect(status().isForbidden());

        server.verify();
    }
}
