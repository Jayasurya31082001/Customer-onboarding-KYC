package com.example.documentservice.document.dto;

import java.util.UUID;

public record DocumentMetadataResponse(
        UUID documentId,
        UUID customerId,
        String fileName,
        String contentType,
        long sizeInBytes
) {
}

