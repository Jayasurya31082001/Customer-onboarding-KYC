package com.example.kycservice.kyc.service;

import com.example.kycservice.kyc.dto.StartKycRequest;

public interface KycVerificationService {

    void startKyc(StartKycRequest request, String idempotencyKey);
}
