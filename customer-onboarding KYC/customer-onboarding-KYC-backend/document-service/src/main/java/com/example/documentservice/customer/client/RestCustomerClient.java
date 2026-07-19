package com.example.documentservice.customer.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
    public boolean customerExists(UUID customerId) {
        RestClient client = restClientBuilder.baseUrl(customerServiceBaseUrl).build();
        try {
            client.get()
                    .uri("/api/v1/customers/{customerId}", customerId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw exception;
        }
    }
}
