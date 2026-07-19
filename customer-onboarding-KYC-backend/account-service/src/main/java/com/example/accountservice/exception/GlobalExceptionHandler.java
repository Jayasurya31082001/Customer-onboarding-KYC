package com.example.accountservice.exception;

import java.net.URI;
import java.time.LocalDateTime;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.bank.com/problems/validation-error"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("correlationId", MDC.get("correlationId"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception,
                                                        HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://api.bank.com/problems/not-found"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("correlationId", MDC.get("correlationId"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled exception. path={}, correlationId={}", request.getRequestURI(), MDC.get("correlationId"), exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal error occurred. Please contact support with correlation ID."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.bank.com/problems/internal-error"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("correlationId", MDC.get("correlationId"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
