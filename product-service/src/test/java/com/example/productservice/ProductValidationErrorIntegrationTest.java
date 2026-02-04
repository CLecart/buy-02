package com.example.productservice;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
 
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

 
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@org.springframework.context.annotation.Import({TestSecurityConfig.class, com.example.shared.web.ApiExceptionHandler.class})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
public class ProductValidationErrorIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    static String TEST_JWT_SECRET;

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        TEST_JWT_SECRET = java.util.HexFormat.of().formatHex(secretBytes);
        reg.add("APP_JWT_SECRET", () -> TEST_JWT_SECRET);
    }


    @org.springframework.beans.factory.annotation.Autowired
    org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.beans.factory.annotation.Autowired
    com.example.shared.security.JwtService jwtService;

    @Autowired
    ProductRepository productRepository;

    @AfterEach
    void after() {
        productRepository.deleteAll();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "SELLER")
    void post_invalid_product_returns_validation_error_response() throws Exception {
        ProductDto req = new ProductDto(null, "", "desc", null);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req);

        var mvcResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/products")
                .contentType(java.util.Objects.requireNonNull(org.springframework.http.MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(json)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assertThat(body).isNotBlank();
        assertThat(body).contains("validation_error").contains("name").contains("price");
    }
}
