package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * API client for the Risk Service (port 8084).
 *
 * <p>Covers:
 * <ul>
 *   <li>POST /api/internal/events/kyc-completed              – ingest KYC-completed event and run risk scoring</li>
 *   <li>GET  /api/v1/risk-assessments/customer/{id}/latest   – query latest risk assessment for a customer</li>
 * </ul>
 *
 * <p>Disposition values from the production code: AUTO_APPROVE | MANUAL_REVIEW | AUTO_REJECT
 * KycStatus serialised values: "Pass" | "Fail" | "KYC_InProgress"
 */
public class RiskApiClient {

    private final RequestSpecification spec;

    public RiskApiClient() {
        this.spec = BaseApiConfig.riskServiceSpec();
    }

    // ─── KYC-Completed Event ──────────────────────────────────────────────────

    /**
     * Sends a kyc-completed event to the Risk Service, triggering automated risk assessment.
     * Maps to: POST /api/internal/events/kyc-completed  → HTTP 202 Accepted
     *
     * @param customerId     UUID of the customer
     * @param kycCaseId      UUID of the KYC case
     * @param documentId     UUID of the uploaded document
     * @param kycStatus      serialised KYC status: "Pass" | "Fail" | "KYC_InProgress"
     * @param nationality    ISO-3166-1 alpha-2 nationality code
     * @param dateOfBirth    customer date of birth
     * @param pepMatch       whether the customer matched a PEP list
     * @param sanctionsMatch whether the customer matched a sanctions list
     * @param customerEmail  customer e-mail address
     * @param correlationId  optional tracing correlation ID
     */
    public Response sendKycCompletedEvent(String customerId,
                                          String kycCaseId,
                                          String documentId,
                                          String kycStatus,
                                          String nationality,
                                          LocalDate dateOfBirth,
                                          boolean pepMatch,
                                          boolean sanctionsMatch,
                                          String customerEmail,
                                          String correlationId) {

        Map<String, Object> body = new HashMap<>();
        body.put("customerId",     customerId);
        body.put("kycCaseId",      kycCaseId);
        body.put("documentId",     documentId);
        body.put("status",         kycStatus);
        body.put("occurredAt",     LocalDateTime.now().toString());
        body.put("correlationId",  correlationId != null ? correlationId : UUID.randomUUID().toString());
        body.put("nationality",    nationality);
        body.put("dateOfBirth",    dateOfBirth != null ? dateOfBirth.toString() : null);
        body.put("pepMatch",       pepMatch);
        body.put("sanctionsMatch", sanctionsMatch);
        body.put("customerEmail",  customerEmail);

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/internal/events/kyc-completed");
    }

    /**
     * Convenience builder: sends a kyc-completed event for a LOW-RISK profile.
     * Expected outcome: Disposition = AUTO_APPROVE.
     *
     * @param customerId UUID of the customer
     * @param email      customer e-mail
     */
    public Response sendLowRiskKycCompleted(String customerId, String email) {
        return sendKycCompletedEvent(
                customerId,
                UUID.randomUUID().toString(),   // kycCaseId
                UUID.randomUUID().toString(),   // documentId
                "Pass",                         // KYC status
                "GB",                           // low-risk nationality
                LocalDate.of(1985, 3, 20),      // > 18 years old
                false,                          // pepMatch = false
                false,                          // sanctionsMatch = false
                email,
                null
        );
    }

    /**
     * Convenience builder: sends a kyc-completed event for a HIGH-RISK profile.
     * High-risk nationalities (from risk-service config): IR, KP, SY.
     * Expected outcome: Disposition = MANUAL_REVIEW.
     *
     * @param customerId UUID of the customer
     * @param email      customer e-mail
     */
    public Response sendHighRiskKycCompleted(String customerId, String email) {
        return sendKycCompletedEvent(
                customerId,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Pass",
                "IR",                           // high-risk nationality
                LocalDate.of(1985, 3, 20),
                false,
                false,
                email,
                null
        );
    }

    /**
     * Convenience builder: sends a kyc-completed event for a REJECTED/FAIL KYC outcome.
     * Expected outcome: Disposition = AUTO_REJECT.
     *
     * @param customerId UUID of the customer
     * @param email      customer e-mail
     */
    public Response sendFailedKycCompleted(String customerId, String email) {
        return sendKycCompletedEvent(
                customerId,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Fail",                         // KYC failed
                "GB",
                LocalDate.of(1985, 3, 20),
                false,
                false,
                email,
                null
        );
    }

    // ─── Risk Query ───────────────────────────────────────────────────────────

    /**
     * Queries the latest risk assessment for a given customer.
     * Maps to: GET /api/v1/risk-assessments/customer/{customerId}/latest
     */
    public Response getLatestRiskAssessment(String customerId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/v1/risk-assessments/customer/{customerId}/latest", customerId);
    }
}
