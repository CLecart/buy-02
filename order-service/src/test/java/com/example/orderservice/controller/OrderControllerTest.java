package com.example.orderservice.controller;

import com.example.shared.dto.CreateOrderRequest;
import com.example.shared.dto.OrderDTO;
import com.example.shared.model.OrderStatus;
import com.example.shared.model.PaymentMethod;
import com.example.shared.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    private com.example.orderservice.controller.OrderController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new OrderController(orderService);
        mapper.registerModule(new JavaTimeModule());
        var converter = new MappingJackson2HttpMessageConverter(mapper);
        mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter).build();
    }

    @Test
    void getOrder_returnsOrder() throws Exception {
        var orderDto = new OrderDTO(
                "order-1",
                "buyer-1",
                "buyer@example.com",
                List.of(new OrderDTO.OrderItemDTO("prod-1", "seller-1", "Product 1", 1, new BigDecimal("10.00"), new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                OrderStatus.PENDING,
                PaymentMethod.CREDIT_CARD,
                "ref-1",
                "Address 1",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(orderService.getOrderById("order-1")).thenReturn(orderDto);

        // Set authenticated user to buyer-1 so enforceOrderAccess passes
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer-1", null));

        String orderJson = Objects.requireNonNull(mapper.writeValueAsString(orderDto));

        mvc.perform(get("/api/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(orderJson));
    }

    @Test
    void createOrder_returnsCreated() throws Exception {
        var item = new CreateOrderRequest.OrderItemRequest(
                "prod-1",
                "seller-1",
                "Product 1",
                2,
                new BigDecimal("5.00")
        );

        var req = new CreateOrderRequest(
                "buyer-1",
                "buyer@example.com",
                List.of(item),
                PaymentMethod.PAY_ON_DELIVERY,
                "",
                "Address 1"
        );

        var created = new OrderDTO(
                "order-2",
                req.buyerId(),
                req.buyerEmail(),
                List.of(new OrderDTO.OrderItemDTO("prod-1", "seller-1", "Product 1", 2, new BigDecimal("5.00"), new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                OrderStatus.PENDING,
                req.paymentMethod(),
                req.paymentReference(),
                req.shippingAddress(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(orderService.createOrder(Mockito.any(CreateOrderRequest.class))).thenReturn(created);

        // Authenticate as buyer-1 so controller uses that as buyerId
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer-1", null));

        String reqJson = Objects.requireNonNull(mapper.writeValueAsString(req));
        String createdJson = Objects.requireNonNull(mapper.writeValueAsString(created));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(reqJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(createdJson));
    }
}
