package com.example.documentservice.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "DocumentUploadRequest", description = "Multipart request used to upload a KYC document")
public record DocumentUploadRequest(
        @Schema(description = "Customer identifier that owns the uploaded document",
                example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
                format = "uuid",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID customerId,

        @Schema(description = "Document file in PDF, JPEG, or PNG format up to 5 MB",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String file
) {
}
