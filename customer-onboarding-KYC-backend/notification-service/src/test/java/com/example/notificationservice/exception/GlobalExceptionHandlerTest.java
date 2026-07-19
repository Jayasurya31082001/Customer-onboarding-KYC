package com.example.notificationservice.exception;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MDC.put("correlationId", "corr-notification");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleValidation_returnsBadRequest() throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", SamplePayload.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SamplePayload(), "samplePayload");
        bindingResult.addError(new FieldError("samplePayload", "templateKey", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidation(ex, new MockHttpServletRequest("POST", "/api/v1/notifications"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("templateKey");
    }

    @Test
    void handleNotFound_returnsNotFound() {
        var response = handler.handleNotFound(
                new ResourceNotFoundException("template missing"),
                new MockHttpServletRequest("GET", "/api/v1/notifications/templates"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("template missing");
    }

    @Test
    void handleGeneric_returnsInternalServerError() {
        var response = handler.handleGeneric(
                new RuntimeException("boom"),
                new MockHttpServletRequest("POST", "/api/v1/notifications"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("internal error occurred");
    }

    @SuppressWarnings("unused")
    private void sampleValidatedMethod(@Valid SamplePayload payload) {
    }

    private static final class SamplePayload {
        private String templateKey;

        public String getTemplateKey() {
            return templateKey;
        }

        public void setTemplateKey(String templateKey) {
            this.templateKey = templateKey;
        }
    }
}
