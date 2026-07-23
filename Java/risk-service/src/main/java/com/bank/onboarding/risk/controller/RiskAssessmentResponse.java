package com.bank.onboarding.risk.controller;

import java.time.LocalDateTime;

import com.bank.onboarding.risk.model.Disposition;

public record RiskAssessmentResponse(
        String assessmentId,
        String customerId,
        int score,
        Disposition disposition,
        LocalDateTime assessedAt
) {
}
