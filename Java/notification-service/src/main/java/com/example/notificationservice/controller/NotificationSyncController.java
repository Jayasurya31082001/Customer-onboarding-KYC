package com.example.notificationservice.controller;

import com.example.notificationservice.model.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
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
 * Internal sync endpoint for notification-service.
 * Consumed exclusively by data-sync-service.
 */
@RestController
@RequestMapping("/internal")
public class NotificationSyncController {

    private static final Logger log = LoggerFactory.getLogger(NotificationSyncController.class);

    private final NotificationLogRepository notificationLogRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public NotificationSyncController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
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

        Page<NotificationLog> logs = notificationLogRepository
                .findByCreatedAtAfterOrUpdatedAtAfter(
                        lastSyncTime, lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("createdAt").ascending()));

        List<Map<String, Object>> content = logs.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          logs.getNumber());
        response.put("size",          logs.getSize());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages",    logs.getTotalPages());
        response.put("last",          logs.isLast());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSyncRecord(NotificationLog notificationLog) {
        Map<String, Object> record = new HashMap<>();
        record.put("notificationId", String.valueOf(notificationLog.getNotificationId()));
        record.put("customerId",     notificationLog.getCustomerId());
        record.put("channel",         notificationLog.getChannel().name());
        record.put("status",          notificationLog.getStatus());
        record.put("eventType",      notificationLog.getTemplateKey());
        record.put("sentAt",         notificationLog.getCreatedAt()); // Map created_at to sentAt
        return record;
    }
}
