package com.example.kycservice.kyc.provider;

public record KycResult(Status status, String reason) {

    public enum Status {
        PASS,
        FAIL,
        REFER
    }
}