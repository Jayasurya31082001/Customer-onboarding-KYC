package com.example.kycservice.document.client;

import java.util.UUID;

public record DocumentDetails(
        UUID documentId,
        UUID customerId,
        String fileName,
        String contentType,
        long sizeInBytes
) {
}

