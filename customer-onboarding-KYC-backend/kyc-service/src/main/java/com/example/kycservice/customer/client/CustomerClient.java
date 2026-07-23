package com.example.kycservice.customer.client;

import java.util.UUID;

public interface CustomerClient {

    CustomerProfile getCustomer(UUID customerId);

    void updateOnboardingStatus(UUID customerId, String onboardingStatus, String correlationId);
}
