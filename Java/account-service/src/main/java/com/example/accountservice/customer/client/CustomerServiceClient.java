package com.example.accountservice.customer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.accountservice.exception.DownstreamIntegrationException;

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

    public String getCustomerEmail(String customerId) {
        try {
            RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
            CustomerEmailResponse response = client.get()
                    .uri("/api/v1/customers/{customerId}/email", customerId)
                    .retrieve()
                    .body(CustomerEmailResponse.class);
            
            if (response != null && response.email() != null && !response.email().isBlank()) {
                log.debug("Retrieved customer email for customerId={}", customerId);
                return response.email();
            }
            throw new DownstreamIntegrationException(
                    "Customer email response was empty",
                    new IllegalStateException("Missing customer email response"));
        } catch (RestClientException e) {
            log.error("Failed to retrieve customer email for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to retrieve customer email", e);
        }
    }

    public void updateOnboardingStatusToApproved(String customerId) {
        try {
            RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
            client.post()
                    .uri("/api/internal/events/account-approved")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateOnboardingStatusRequest(customerId, "APPROVED"))
                    .retrieve()
                    .toBodilessEntity();
            
            log.debug("Updated onboarding status to APPROVED for customerId={}", customerId);
        } catch (RestClientException e) {
            log.error("Failed to update onboarding status to APPROVED for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to update onboarding status to APPROVED", e);
        }
    }

    public record CustomerEmailResponse(String email) {}

    private record UpdateOnboardingStatusRequest(String customerId, String status) {}
}
