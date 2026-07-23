package com.example.kycservice.kyc.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KycStatus {
    KYC_IN_PROGRESS("KYC_InProgress"),
    PASS("Pass"),
    FAIL("Fail");

    private final String apiValue;

    KycStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }
}
