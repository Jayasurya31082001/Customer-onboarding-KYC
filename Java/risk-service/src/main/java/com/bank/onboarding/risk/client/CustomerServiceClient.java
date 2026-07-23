package com.bank.onboarding.risk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.bank.onboarding.risk.exception.DownstreamIntegrationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

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
           updateOnboardingStatus(UUID.fromString(customerId), "MANUAL_APPROVAL_REQUIRED");

            log.info("Updated onboarding status to MANUAL_APPROVAL_REQUIRED for customerId={}", customerId);
        } catch (RestClientException e) {
            log.error("Failed to update onboarding status for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to update onboarding status to MANUAL_APPROVAL_REQUIRED", e);
        }
    }

    public void updateOnboardingStatusToRejected(String customerId) {
        try {
             updateOnboardingStatus(UUID.fromString(customerId), "REJECTED");
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
    private void updateOnboardingStatus(UUID customerId, String onboardingStatus) {
        RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
        try {
            client.patch()
                    .uri("/api/v1/customers/{customerId}/status", customerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateCustomerStatusRequest(onboardingStatus))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Customer not found: " + customerId);
            }
            throw exception;
        }
    }

    private record UpdateCustomerStatusRequest(String status) {
    }

    public record CustomerProfile(String customerId, java.time.LocalDate dateOfBirth, String nationality, String email) {}
}
