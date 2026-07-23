package com.example.kycservice.kyc.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kycservice.kyc.model.KycCase;

public interface KycCaseRepository extends JpaRepository<KycCase, UUID> {

    Optional<KycCase> findByIdempotencyKey(String idempotencyKey);

    Optional<KycCase> findTopByCustomerIdOrderByUpdatedAtDesc(UUID customerId);

    /** Incremental sync query — returns KYC cases created or updated after the cursor. */
    Page<KycCase> findByCreatedAtAfterOrUpdatedAtAfter(
            LocalDateTime createdAfter, LocalDateTime updatedAfter, Pageable pageable);
}
