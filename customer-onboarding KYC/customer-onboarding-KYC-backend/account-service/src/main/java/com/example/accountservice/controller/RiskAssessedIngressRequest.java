package com.example.accountservice.controller;

import com.example.accountservice.model.Disposition;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RiskAssessedIngressRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email address")
        String customerEmail,

        @NotNull(message = "disposition is required")
        Disposition disposition,

        int score
) {
}
