package com.example.kycservice.risk.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.kycservice.kyc.model.KycStatus;

@Component
public class RestRiskClient implements RiskClient {

    private final RestClient.Builder restClientBuilder;
    private final String riskServiceBaseUrl;

    public RestRiskClient(RestClient.Builder restClientBuilder,
                          @Value("${risk.service.base-url}") String riskServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.riskServiceBaseUrl = riskServiceBaseUrl;
    }

    @Override
    public void publishKycCompleted(UUID kycCaseId,
                                    UUID customerId,
                                    UUID documentId,
                                    KycStatus status,
                                    LocalDateTime occurredAt,
                                    String correlationId,
                                    String nationality,
                                    LocalDate dateOfBirth,
                                    boolean pepMatch,
                                    boolean sanctionsMatch,
                                    String customerEmail) {
        RestClient client = restClientBuilder.baseUrl(riskServiceBaseUrl).build();

        RestClient.RequestBodySpec requestSpec = client.post()
                .uri("/api/internal/events/kyc-completed")
                .contentType(MediaType.APPLICATION_JSON);

        if (correlationId != null && !correlationId.isBlank()) {
            requestSpec = requestSpec.header("X-Correlation-ID", correlationId);
        }

        requestSpec
                .body(new KycCompletedRiskIngressRequest(
                        kycCaseId,
                        customerId,
                        documentId,
                        status,
                        occurredAt,
                        correlationId,
                        nationality,
                        dateOfBirth,
                        pepMatch,
                        sanctionsMatch,
                        customerEmail
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private record KycCompletedRiskIngressRequest(
            UUID kycCaseId,
            UUID customerId,
            UUID documentId,
            KycStatus status,
            LocalDateTime occurredAt,
            String correlationId,
            String nationality,
            java.time.LocalDate dateOfBirth,
            boolean pepMatch,
            boolean sanctionsMatch,
            String customerEmail
    ) {
    }
}
