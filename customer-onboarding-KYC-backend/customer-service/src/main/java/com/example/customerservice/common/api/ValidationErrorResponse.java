package com.example.customerservice.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(name = "ValidationErrorResponse", description = "Problem response for validation failures with field-level messages")
public record ValidationErrorResponse(
        @Schema(description = "Problem type URI", example = "https://example.com/problems/validation-failed")
        String type,

        @Schema(description = "Short human-readable error title", example = "Validation failed")
        String title,

        @Schema(description = "HTTP status code", example = "400")
        Integer status,

        @Schema(description = "Detailed error message", example = "One or more request fields are invalid")
        String detail,

        @Schema(description = "Request path that caused the error", example = "/api/v1/customers")
        String path,

        @Schema(description = "Timestamp of the error response", example = "2026-07-09T22:45:21")
        LocalDateTime timestamp,

        @Schema(description = "Field-level validation messages")
        Map<String, String> errors
) {
}

