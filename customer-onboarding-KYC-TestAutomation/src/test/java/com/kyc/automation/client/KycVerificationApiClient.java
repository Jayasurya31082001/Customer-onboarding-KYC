package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * API client for the KYC Service (port 8083).
 *
 * <p>KYC verification is event-driven via internal ingress endpoints:
 * <ul>
 *   <li>POST /api/internal/events/customer-registered – triggers KYC case creation</li>
 *   <li>POST /api/internal/events/document-uploaded   – triggers KYC verification run</li>
 * </ul>
 *
 * <p>KYC outcomes (Pass / Fail / KYC_InProgress) are then propagated downstream to
 * the risk-service via its own ingress endpoint (see {@link RiskApiClient}).
 */
public class KycVerificationApiClient {

    private final RequestSpecification spec;

    public KycVerificationApiClient() {
        this.spec = BaseApiConfig.kycServiceSpec();
    }

    // ─── KYC Ingress Events ───────────────────────────────────────────────────

    /**
     * Sends a customer-registered event to the KYC service to initiate a KYC case.
     * Maps to: POST /api/internal/events/customer-registered
     *
     * @param customerId  UUID of the newly registered customer
     * @param correlationId optional correlation header (may be null)
     * @return raw RestAssured response (expect HTTP 202 Accepted)
     */
    public Response sendCustomerRegisteredEvent(String customerId, String correlationId) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerId",     customerId);
        body.put("occurredAt",     LocalDateTime.now().toString());
        body.put("correlationId",  correlationId != null ? correlationId : UUID.randomUUID().toString());

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/internal/events/customer-registered");
    }

    /**
     * Sends a document-uploaded event to the KYC service to kick off identity verification.
     * Maps to: POST /api/internal/events/document-uploaded
     *
     * @param customerId  UUID of the customer who uploaded the document
     * @param documentId  UUID of the uploaded document
     * @param correlationId optional correlation header
     * @return raw RestAssured response (expect HTTP 202 Accepted)
     */
    public Response sendDocumentUploadedEvent(String customerId,
                                              String documentId,
                                              String correlationId) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerId",    customerId);
        body.put("documentId",    documentId);
        body.put("occurredAt",    LocalDateTime.now().toString());
        body.put("correlationId", correlationId != null ? correlationId : UUID.randomUUID().toString());

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/internal/events/document-uploaded");
    }

    /**
     * Sends an invalid (missing required fields) customer-registered event — used for
     * negative testing of the KYC ingress validation.
     */
    public Response sendInvalidCustomerRegisteredEvent() {
        // Missing required customerId and occurredAt fields
        Map<String, Object> body = new HashMap<>();
        body.put("correlationId", "bad-correlation");

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/internal/events/customer-registered");
    }
}
