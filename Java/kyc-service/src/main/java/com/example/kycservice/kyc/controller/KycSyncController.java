package com.example.kycservice.kyc.controller;

import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.repository.KycCaseRepository;
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
 * Internal sync endpoint for kyc-service.
 * Consumed exclusively by data-sync-service.
 */
@RestController
@RequestMapping("/internal")
public class KycSyncController {

    private static final Logger log = LoggerFactory.getLogger(KycSyncController.class);

    private final KycCaseRepository kycCaseRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public KycSyncController(KycCaseRepository kycCaseRepository) {
        this.kycCaseRepository = kycCaseRepository;
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

        Page<KycCase> cases = kycCaseRepository
                .findByCreatedAtAfterOrUpdatedAtAfter(
                        lastSyncTime, lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("createdAt").ascending()));

        List<Map<String, Object>> content = cases.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          cases.getNumber());
        response.put("size",          cases.getSize());
        response.put("totalElements", cases.getTotalElements());
        response.put("totalPages",    cases.getTotalPages());
        response.put("last",          cases.isLast());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSyncRecord(KycCase kycCase) {
        Map<String, Object> record = new HashMap<>();
        record.put("kycCaseId",   kycCase.getKycCaseId().toString());
        record.put("customerId",   kycCase.getCustomerId().toString());
        record.put("documentId",   kycCase.getDocumentId().toString());
        record.put("status",       kycCase.getStatus().name());
        record.put("notes",        kycCase.getNotes());
        record.put("createdAt",    kycCase.getCreatedAt());
        record.put("updatedAt",    kycCase.getUpdatedAt());
        return record;
    }
}
