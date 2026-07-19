package com.example.kycservice.kyc.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.kycservice.common.exception.ResourceNotFoundException;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@Service
public class KycStatusService {

    private final KycCaseRepository kycCaseRepository;

    public KycStatusService(KycCaseRepository kycCaseRepository) {
        this.kycCaseRepository = kycCaseRepository;
    }

    public KycStatus getKycStatus(UUID customerId) {
        return kycCaseRepository.findTopByCustomerIdOrderByUpdatedAtDesc(customerId)
                .map(kycCase -> kycCase.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("KYC status not found for customerId=" + customerId));
    }
}