package com.example.customerservice.common.controller;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.customerservice.common.exception.DuplicateEmailException;
import com.example.customerservice.common.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleMethodArgumentNotValid_returnsBadRequestWithErrorsMap() throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", SamplePayload.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SamplePayload(), "samplePayload");
        bindingResult.addError(new FieldError("samplePayload", "email", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, new MockHttpServletRequest("POST", "/api/v1/customers"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Validation failed");
        assertThat(response.getProperties()).containsKey("errors");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getProperties().get("errors");
        assertThat(errors).containsEntry("email", "must not be blank");
    }

    @Test
    void handleMethodArgumentTypeMismatch_returnsBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "age",
                null,
                null);

        var response = handler.handleMethodArgumentTypeMismatch(ex, new MockHttpServletRequest("GET", "/api/v1/customers"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).contains("age");
    }

    @Test
    void handleDuplicateEmail_returnsConflict() {
        var response = handler.handleDuplicateEmail(
                new DuplicateEmailException("already exists"),
                new MockHttpServletRequest("POST", "/api/v1/customers"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getDetail()).isEqualTo("already exists");
    }

    @Test
    void handleResourceNotFound_returnsNotFound() {
        var response = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing"),
                new MockHttpServletRequest("GET", "/api/v1/customers/cust-1"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("missing");
    }

    @Test
    void handleUnexpected_returnsInternalServerError() {
        var response = handler.handleUnexpected(
                new RuntimeException("boom"),
                new MockHttpServletRequest("GET", "/api/v1/customers/cust-1"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @SuppressWarnings("unused")
    private void sampleValidatedMethod(@Valid SamplePayload payload) {
    }

    private static final class SamplePayload {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
