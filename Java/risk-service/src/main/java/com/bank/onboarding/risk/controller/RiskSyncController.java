package com.bank.onboarding.risk.controller;

import com.bank.onboarding.risk.model.RiskAssessment;
import com.bank.onboarding.risk.repository.RiskAssessmentRepository;
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
 * Internal sync endpoint for risk-service.
 * Consumed exclusively by data-sync-service.
 */
@RestController
@RequestMapping("/internal")
public class RiskSyncController {

    private static final Logger log = LoggerFactory.getLogger(RiskSyncController.class);

    private final RiskAssessmentRepository riskAssessmentRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public RiskSyncController(RiskAssessmentRepository riskAssessmentRepository) {
        this.riskAssessmentRepository = riskAssessmentRepository;
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

        // Find assessments since lastSyncTime. Since risk assessments are immutable after creation,
        // we can filter using assessedAt.
        Page<RiskAssessment> assessments = riskAssessmentRepository
                .findByAssessedAtAfter(
                        lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("assessedAt").ascending()));

        List<Map<String, Object>> content = assessments.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          assessments.getNumber());
        response.put("size",          assessments.getSize());
        response.put("totalElements", assessments.getTotalElements());
        response.put("totalPages",    assessments.getTotalPages());
        response.put("last",          assessments.isLast());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSyncRecord(RiskAssessment assessment) {
        Map<String, Object> record = new HashMap<>();
        record.put("riskId",       assessment.getAssessmentId());
        record.put("customerId",   assessment.getCustomerId());
        record.put("riskScore",    String.valueOf(assessment.getScore()));
        
        // Derive category from score
        String category = "LOW";
        if (assessment.getScore() >= 70) {
            category = "HIGH";
        } else if (assessment.getScore() >= 35) {
            category = "MEDIUM";
        }
        record.put("riskCategory", category);
        record.put("decision",      assessment.getDisposition().name());
        record.put("assessedAt",   assessment.getAssessedAt());
        return record;
    }
}
