package com.example.documentservice.common.controller;

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

import com.example.documentservice.common.exception.DocumentStorageException;
import com.example.documentservice.common.exception.InvalidDocumentException;
import com.example.documentservice.common.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MDC.put("correlationId", "corr-doc");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleMethodArgumentNotValid_returnsBadRequest() throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", SamplePayload.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SamplePayload(), "samplePayload");
        bindingResult.addError(new FieldError("samplePayload", "documentType", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, new MockHttpServletRequest("POST", "/api/v1/documents"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Validation failed");
        assertThat(response.getProperties()).containsKey("errors");
        assertThat(response.getProperties()).containsEntry("correlationId", "corr-doc");
    }

    @Test
    void handleResourceNotFound_returnsNotFound() {
        var response = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing"),
                new MockHttpServletRequest("GET", "/api/v1/documents/1"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("missing");
    }

    @Test
    void handleInvalidDocument_returnsBadRequest() {
        var response = handler.handleInvalidDocument(
                new InvalidDocumentException("invalid"),
                new MockHttpServletRequest("POST", "/api/v1/documents"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("invalid");
    }

    @Test
    void handleDocumentStorage_returnsInternalServerError() {
        var response = handler.handleDocumentStorage(
                new DocumentStorageException("store failed", new RuntimeException("io")),
                new MockHttpServletRequest("POST", "/api/v1/documents"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("store failed");
    }

    @Test
    void handleUnexpected_returnsInternalServerError() {
        var response = handler.handleUnexpected(
                new RuntimeException("boom"),
                new MockHttpServletRequest("GET", "/api/v1/documents"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @SuppressWarnings("unused")
    private void sampleValidatedMethod(@Valid SamplePayload payload) {
    }

    private static final class SamplePayload {
        private String documentType;

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }
    }
}
