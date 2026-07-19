package com.example.documentservice.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadResponse(
        @Schema(description = "Generated document identifier",
                example = "c50e0f65-fdc5-4b0c-bf6a-60287f6430c7",
                format = "uuid")
        UUID documentId,

        @Schema(description = "Customer identifier associated with the document",
                example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
                format = "uuid")
        UUID customerId,

        @Schema(description = "Original file name", example = "passport.pdf")
        String fileName,

        @Schema(description = "Uploaded file content type", example = "application/pdf")
        String contentType,

        @Schema(description = "File size in bytes", example = "245678")
        long sizeInBytes,

        @Schema(description = "Timestamp when the document was stored", example = "2026-07-09T22:45:21")
        LocalDateTime createdAt
) {
}
