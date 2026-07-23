package com.example.documentservice.document.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.documentservice.document.event.DocumentUploadedEvent;
import com.example.documentservice.kyc.client.DocumentUploadedIngressRequest;

class DtoAndEventRecordsTest {

    @Test
    void documentUploadRequest_storesFields() {
        UUID customerId = UUID.randomUUID();
        DocumentUploadRequest request = new DocumentUploadRequest(customerId, "binary");

        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.file()).isEqualTo("binary");
    }

    @Test
    void documentUploadResponse_storesFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        DocumentUploadResponse response = new DocumentUploadResponse(documentId, customerId, "passport.pdf", "application/pdf", 200L, createdAt);

        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void documentMetadataResponse_storesFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        DocumentMetadataResponse response = new DocumentMetadataResponse(documentId, customerId, "passport.pdf", "application/pdf", 200L);

        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.fileName()).isEqualTo("passport.pdf");
    }

    @Test
    void documentUploadedEvent_storesFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        DocumentUploadedEvent event = new DocumentUploadedEvent(documentId, customerId, "passport.pdf", "application/pdf", occurredAt, "corr-1");

        assertThat(event.documentId()).isEqualTo(documentId);
        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void documentUploadedIngressRequest_storesFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        DocumentUploadedIngressRequest request = new DocumentUploadedIngressRequest(documentId, customerId, occurredAt, "corr-1");

        assertThat(request.documentId()).isEqualTo(documentId);
        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.correlationId()).isEqualTo("corr-1");
    }
}
