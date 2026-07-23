package com.example.customerservice.kyc.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KycEventPublisherClient {

    private final RestClient.Builder restClientBuilder;
    private final String kycServiceBaseUrl;

    public KycEventPublisherClient(RestClient.Builder restClientBuilder,
                                   @Value("${kyc.service.base-url}") String kycServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.kycServiceBaseUrl = kycServiceBaseUrl;
    }

    public void publishCustomerRegistered(CustomerRegisteredIngressRequest request) {
        RestClient client = restClientBuilder.baseUrl(kycServiceBaseUrl).build();
        client.post()
                .uri("/api/internal/events/customer-registered")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
