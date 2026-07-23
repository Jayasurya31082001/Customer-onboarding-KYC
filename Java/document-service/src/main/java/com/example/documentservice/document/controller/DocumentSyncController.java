package com.example.documentservice.document.controller;

import com.example.documentservice.document.model.CustomerDocument;
import com.example.documentservice.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal sync endpoint for document-service.
 * Consumed exclusively by data-sync-service.
 */
@RestController
@RequestMapping("/internal")
public class DocumentSyncController {

    private static final Logger log = LoggerFactory.getLogger(DocumentSyncController.class);

    private final DocumentRepository documentRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public DocumentSyncController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping("/sync")
    public ResponseEntity<?> getIncrementalChanges(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSyncTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestHeader("X-Internal-Secret") String secret) {

        if (!expectedSecret.equals(secret)) {
            log.warn("Rejected /internal/sync request — invalid X-Internal-Secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        int effectiveSize = Math.min(size, 1000);

        log.debug("Internal sync requested: lastSyncTime={}, page={}, size={}", lastSyncTime, page, effectiveSize);

        Page<CustomerDocument> documents = documentRepository
                .findByCreatedAtAfterOrUpdatedAtAfter(
                        lastSyncTime, lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("createdAt").ascending()));

        List<Map<String, Object>> content = documents.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          documents.getNumber());
        response.put("size",          documents.getSize());
        response.put("totalElements", documents.getTotalElements());
        response.put("totalPages",    documents.getTotalPages());
        response.put("last",          documents.isLast());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSyncRecord(CustomerDocument doc) {
        Map<String, Object> record = new HashMap<>();
        record.put("documentId",   doc.getDocumentId().toString());
        record.put("customerId",   doc.getCustomerId().toString());
        record.put("documentType", doc.getContentType());
        record.put("status",        "UPLOADED");
        record.put("uploadedAt",   doc.getCreatedAt());
        record.put("verifiedAt",   doc.getUpdatedAt());
        return record;
    }
}
