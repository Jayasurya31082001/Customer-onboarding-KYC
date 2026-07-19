package com.example.customerservice.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ApiErrorResponse", description = "Standard problem response returned for API errors")
public record ApiErrorResponse(
        @Schema(description = "Problem type URI", example = "https://example.com/problems/duplicate-email")
        String type,

        @Schema(description = "Short human-readable error title", example = "Duplicate email")
        String title,

        @Schema(description = "HTTP status code", example = "409")
        Integer status,

        @Schema(description = "Detailed error message", example = "Email address already registered")
        String detail,

        @Schema(description = "Request path that caused the error", example = "/api/v1/customers")
        String path,

        @Schema(description = "Timestamp of the error response", example = "2026-07-09T22:45:21")
        LocalDateTime timestamp
) {
}

