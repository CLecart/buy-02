package com.example.orderservice;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.9");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void createAndGetOrder() {
        var item = new CreateOrderRequest.OrderItemRequest(
                "prod-int-1",
                "seller-int-1",
                "Integration Product",
                1,
                new BigDecimal("12.34")
        );

        var req = new CreateOrderRequest(
                "buyer-int-1",
                "buyer@int.test",
                List.of(item),
                PaymentMethod.PAY_ON_DELIVERY,
                "",
                "Integration Address"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<OrderDTO> createResp = restTemplate.postForEntity(
                url("/api/orders"), entity, OrderDTO.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderDTO created = Objects.requireNonNull(createResp.getBody(), "create response body");
        assertThat(created.buyerId()).isEqualTo(req.buyerId());

        ResponseEntity<OrderDTO> getResp = restTemplate.getForEntity(
            url("/api/orders/" + created.id()), OrderDTO.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        OrderDTO fetched = Objects.requireNonNull(getResp.getBody(), "get response body");
        assertThat(fetched.id()).isEqualTo(created.id());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
