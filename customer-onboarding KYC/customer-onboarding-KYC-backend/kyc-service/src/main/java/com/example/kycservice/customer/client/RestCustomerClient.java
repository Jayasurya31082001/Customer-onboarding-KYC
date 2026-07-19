package com.example.kycservice.customer.client;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.kycservice.common.exception.ResourceNotFoundException;

import lombok.Getter;
import lombok.Setter;

@Component
public class RestCustomerClient implements CustomerClient {

    private final RestClient.Builder restClientBuilder;
    private final String customerServiceBaseUrl;

    public RestCustomerClient(RestClient.Builder restClientBuilder,
                              @Value("${customer.service.base-url}") String customerServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.customerServiceBaseUrl = customerServiceBaseUrl;
    }

    @Override
    public CustomerProfile getCustomer(UUID customerId) {
        RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
        try {
            CustomerServiceResponse response = client.get()
                    .uri("/api/v1/customers/{customerId}", customerId)
                    .retrieve()
                    .body(CustomerServiceResponse.class);

            if (response == null) {
                throw new ResourceNotFoundException("Customer not found: " + customerId);
            }

            return new CustomerProfile(
                    response.getCustomerId(),
                    response.getFirstName(),
                    response.getLastName(),
                    response.getDateOfBirth(),
                    response.getNationality(),
                    response.getStatus(),
                    response.getEmail()
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Customer not found: " + customerId);
            }
            throw exception;
        }
    }

    @Override
    public void updateOnboardingStatus(UUID customerId, String onboardingStatus, String correlationId) {
        RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
        try {
            RestClient.RequestBodySpec requestSpec = client.patch()
                    .uri("/api/v1/customers/{customerId}/status", customerId)
                    .contentType(MediaType.APPLICATION_JSON);

            if (correlationId != null && !correlationId.isBlank()) {
                requestSpec = requestSpec.header("X-Correlation-ID", correlationId);
            }

            requestSpec
                    .body(new UpdateCustomerStatusRequest(onboardingStatus))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Customer not found: " + customerId);
            }
            throw exception;
        }
    }

    private record UpdateCustomerStatusRequest(String status) {
    }

    @Getter
    @Setter
    private static class CustomerServiceResponse {
        private UUID customerId;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String nationality;
        private String status;
        private String email;
    }
}
