package com.example.shared.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that ApiExceptionHandler returns the expected JSON shape for validation errors.
 */
public class ApiExceptionHandlerTest {

    static class TestDto {
        @NotNull(message = "name.required")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @org.springframework.web.bind.annotation.RestController
    static class TestController {
        
        @org.springframework.web.bind.annotation.PostMapping("/test")
        public void create(@org.springframework.web.bind.annotation.RequestBody @Valid TestDto dto) {
            
        }
    }

    @Test
    void validationErrorsReturnErrorResponse() throws Exception {
        TestController controller = new TestController();
        ApiExceptionHandler advice = new ApiExceptionHandler();

    org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
    
    validator.setMessageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator());
    validator.afterPropertiesSet();

    MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(advice)
        .setValidator(validator)
        .build();

        
    mvc.perform(post("/test")
            .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"))
                .andExpect(jsonPath("$.message").exists());
    }
}
