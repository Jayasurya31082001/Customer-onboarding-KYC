package com.bank.onboarding.risk.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum KycStatus {
    KYC_IN_PROGRESS("KYC_InProgress"),
    PASS("Pass"),
    FAIL("Fail"),
    REFER("Refer");

    private final String apiValue;

    KycStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    @JsonCreator
    public static KycStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value)
                        || status.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}