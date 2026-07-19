package com.bank.onboarding.risk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.bank.onboarding.risk.exception.DownstreamIntegrationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final String customerServiceBaseUrl;

    public CustomerServiceClient(RestClient.Builder restClientBuilder,
                                 @Value("${customer.service.base-url}") String customerServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.customerServiceBaseUrl = customerServiceBaseUrl;
    }

    public void updateOnboardingStatusToManualReview(String customerId) {
        try {
            RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
            client.post()
                    .uri("/api/internal/events/manual-approval-required")
                    .body(new UpdateOnboardingStatusRequest(customerId, "MANUAL_APPROVAL_REQUIRED"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Updated onboarding status to MANUAL_APPROVAL_REQUIRED for customerId={}", customerId);
        } catch (RestClientException e) {
            log.error("Failed to update onboarding status for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to update onboarding status to MANUAL_APPROVAL_REQUIRED", e);
        }
    }

    public void updateOnboardingStatusToRejected(String customerId) {
        try {
            RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
            client.post()
                    .uri("/api/internal/events/application-rejected")
                    .body(new UpdateOnboardingStatusRequest(customerId, "REJECTED"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Updated onboarding status to REJECTED for customerId={}", customerId);
        } catch (RestClientException e) {
            log.error("Failed to update onboarding status for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to update onboarding status to REJECTED", e);
        }
    }

    public CustomerProfile getCustomerProfile(String customerId) {
        CustomerProfile response;
        try {
            RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
            response = client.get()
                    .uri("/api/v1/customers/{customerId}", customerId)
                    .retrieve()
                    .body(CustomerProfile.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch customer profile for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to fetch customer profile", e);
        }

        if (response != null) {
            return response;
        }

        throw new DownstreamIntegrationException(
                "Customer profile response was empty",
                new IllegalStateException("Empty customer profile response"));
    }

    public record UpdateOnboardingStatusRequest(String customerId, String status) {}

    public record CustomerProfile(String customerId, java.time.LocalDate dateOfBirth, String nationality, String email) {}
}
