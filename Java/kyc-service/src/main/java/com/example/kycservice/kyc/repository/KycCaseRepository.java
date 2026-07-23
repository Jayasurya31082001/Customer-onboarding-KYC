package com.example.kycservice.kyc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kycservice.kyc.model.KycCase;

public interface KycCaseRepository extends JpaRepository<KycCase, UUID> {

	Optional<KycCase> findByIdempotencyKey(String idempotencyKey);

	Optional<KycCase> findTopByCustomerIdOrderByUpdatedAtDesc(UUID customerId);
}
