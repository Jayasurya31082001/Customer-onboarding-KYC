package com.example.customerservice.customer.controller;

import com.example.customerservice.customer.model.Customer;
import com.example.customerservice.customer.repository.CustomerRepository;
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
 * Internal sync endpoint for customer-service.
 *
 * <p>Consumed exclusively by {@code data-sync-service}. Never exposed through
 * the public API gateway. No Databricks-specific code here — this controller
 * knows nothing about where the data goes after leaving this service.
 *
 * <p><b>Security:</b> validated via the {@code X-Internal-Secret} header.
 * In production, additionally restrict this path at the network/ingress level
 * so only the data-sync-service IP can reach it.
 *
 * <p><b>Adding updatedAt to Customer entity:</b>
 * This endpoint requires an {@code updatedAt} field on the Customer entity.
 * Add:
 * <pre>
 *   {@code @LastModifiedDate}
 *   {@code @Column(name = "updated_at")}
 *   private LocalDateTime updatedAt;
 * </pre>
 * and enable {@code @EnableJpaAuditing} in a config class.
 */
@RestController
@RequestMapping("/internal")
public class CustomerSyncController {

    private static final Logger log = LoggerFactory.getLogger(CustomerSyncController.class);

    private final CustomerRepository customerRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public CustomerSyncController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Returns customers created or updated after {@code lastSyncTime}, paginated.
     *
     * <p>Query: {@code updatedAt >= lastSyncTime OR createdAt >= lastSyncTime}
     * with a 60-second overlap already applied by the caller (data-sync-service).
     *
     * @param lastSyncTime ISO-8601 timestamp (e.g. "2026-07-23T08:00:00")
     * @param page         0-based page index
     * @param size         records per page (max 1000)
     * @param secret       must match ${datasync.security.internal-secret}
     */
    @GetMapping("/sync")
    public ResponseEntity<?> getIncrementalChanges(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSyncTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestHeader("X-Internal-Secret") String secret) {

        // ── Auth check ────────────────────────────────────────────────────────
        if (!expectedSecret.equals(secret)) {
            log.warn("Rejected /internal/sync request — invalid X-Internal-Secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        // ── Clamp page size ───────────────────────────────────────────────────
        int effectiveSize = Math.min(size, 1000);

        log.debug("Internal sync requested: lastSyncTime={}, page={}, size={}", lastSyncTime, page, effectiveSize);

        // ── Query ─────────────────────────────────────────────────────────────
        // Requires CustomerRepository to have a method that filters by updatedAt/createdAt.
        // See the repository method below.
        Page<Customer> customers = customerRepository
                .findByCreatedAtAfterOrUpdatedAtAfter(
                        lastSyncTime, lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("createdAt").ascending()));

        // ── Map to sync DTO — expose only analytics fields ────────────────────
        List<Map<String, Object>> content = customers.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          customers.getNumber());
        response.put("size",          customers.getSize());
        response.put("totalElements", customers.getTotalElements());
        response.put("totalPages",    customers.getTotalPages());
        response.put("last",          customers.isLast());

        return ResponseEntity.ok(response);
    }

    /** Maps a Customer entity to a flat analytics-safe sync record. */
    private Map<String, Object> toSyncRecord(Customer customer) {
        Map<String, Object> record = new HashMap<>();
        record.put("customerId",  customer.getCustomerId().toString());
        record.put("firstName",   customer.getFirstName());
        record.put("lastName",    customer.getLastName());
        record.put("email",       customer.getEmail());       // data-sync-service will hash this
        record.put("nationality", customer.getNationality());
        record.put("city",        customer.getCity());
        record.put("postcode",    customer.getPostcode());
        record.put("status",      customer.getStatus().name());
        record.put("createdAt",   customer.getCreatedAt());
        record.put("updatedAt",   customer.getUpdatedAt()); // may be null if not added yet
        return record;
    }
}
