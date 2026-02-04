package com.example.productservice;

import com.example.productservice.controller.ProductController;
import com.example.productservice.service.ProductService;
import com.example.shared.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import java.util.Objects;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

public class ProductValidationErrorMvcTest {

    @Test
    void mvc_post_invalid_product_returns_validation_error_json() throws Exception {
        ProductService svc = Mockito.mock(ProductService.class);
        ProductController controller = new ProductController(svc);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();

        String payload = "{ \"name\": \"\", \"description\": \"desc\" }";

        mvc.perform(post("/api/products")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Objects.requireNonNull(containsString("validation_error"))));
    }
}
