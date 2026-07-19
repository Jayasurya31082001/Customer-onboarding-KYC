package com.example.kycservice.customer.client;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfile(
        UUID customerId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String nationality,
        String onboardingStatus,
        String email
) {
}
