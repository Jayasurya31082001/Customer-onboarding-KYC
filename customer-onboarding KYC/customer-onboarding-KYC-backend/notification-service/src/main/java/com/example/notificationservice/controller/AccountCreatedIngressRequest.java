package com.example.notificationservice.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountCreatedIngressRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email address")
        String customerEmail,

        @NotBlank(message = "accountNumber is required")
        String accountNumber,

        @NotBlank(message = "sortCode is required")
        String sortCode
) {
}
