package com.example.kycservice.kyc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kycservice.kyc.model.KycEventTriggerState;

public interface KycEventTriggerStateRepository extends JpaRepository<KycEventTriggerState, UUID> {
}
