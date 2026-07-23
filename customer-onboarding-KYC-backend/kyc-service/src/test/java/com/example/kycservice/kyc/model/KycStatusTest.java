package com.example.kycservice.kyc.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KycStatusTest {

    @Test
    void apiValues_matchContract() {
        assertThat(KycStatus.KYC_IN_PROGRESS.getApiValue()).isEqualTo("KYC_InProgress");
        assertThat(KycStatus.PASS.getApiValue()).isEqualTo("Pass");
        assertThat(KycStatus.FAIL.getApiValue()).isEqualTo("Fail");
    }
}
