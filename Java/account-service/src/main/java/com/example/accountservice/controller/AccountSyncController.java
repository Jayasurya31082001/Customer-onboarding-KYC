package com.example.accountservice.controller;

import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
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
 * Internal sync endpoint for account-service.
 * Consumed exclusively by data-sync-service.
 */
@RestController
@RequestMapping("/internal")
public class AccountSyncController {

    private static final Logger log = LoggerFactory.getLogger(AccountSyncController.class);

    private final AccountRepository accountRepository;

    @Value("${datasync.security.internal-secret:dev-internal-secret-change-in-prod}")
    private String expectedSecret;

    public AccountSyncController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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

        // Accounts are immutable after creation, so we query using findByCreatedAtAfter.
        Page<Account> accounts = accountRepository
                .findByCreatedAtAfter(
                        lastSyncTime,
                        PageRequest.of(page, effectiveSize, Sort.by("createdAt").ascending()));

        List<Map<String, Object>> content = accounts.getContent().stream()
                .map(this::toSyncRecord)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content",       content);
        response.put("page",          accounts.getNumber());
        response.put("size",          accounts.getSize());
        response.put("totalElements", accounts.getTotalElements());
        response.put("totalPages",    accounts.getTotalPages());
        response.put("last",          accounts.isLast());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSyncRecord(Account account) {
        Map<String, Object> record = new HashMap<>();
        record.put("accountId",     account.getAccountId());
        record.put("customerId",    account.getCustomerId());
        record.put("accountNumber", account.getAccountNumber());
        record.put("sortCode",      account.getSortCode());
        record.put("accountType",   account.getAccountType());
        record.put("status",        account.getStatus());
        record.put("createdAt",     account.getCreatedAt());
        return record;
    }
}
