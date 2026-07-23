package com.example.documentservice.document.dto;

import java.util.UUID;

public record DocumentDownloadResponse(
        UUID documentId,
        UUID customerId,
        String fileName,
        String contentType,
        long sizeInBytes,
        byte[] content
) {
}
