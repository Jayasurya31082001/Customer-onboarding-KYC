package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * API client for the Account Service (port 8085).
 *
 * <p>Covers:
 * <ul>
 *   <li>POST /api/internal/events/risk-assessed          – ingest risk-assessed event
 *                                                           → triggers account provisioning</li>
 *   <li>GET  /api/v1/accounts/customer/{customerId}      – query account by customer ID</li>
 * </ul>
 *
 * <p>The Account Service provisions a bank account automatically when risk disposition is
 * AUTO_APPROVE. AUTO_REJECT causes the account provisioning to be skipped.
 */
public class AccountApiClient {

    private final RequestSpecification spec;

    public AccountApiClient() {
        this.spec = BaseApiConfig.accountServiceSpec();
    }

    // ─── Risk-Assessed Event ──────────────────────────────────────────────────

    /**
     * Sends a risk-assessed event to the Account Service.
     * Maps to: POST /api/internal/events/risk-assessed  → HTTP 202 Accepted
     *
     * <p>The {@code disposition} field controls whether an account is provisioned:
     * <ul>
     *   <li>AUTO_APPROVE → account created</li>
     *   <li>MANUAL_REVIEW → no account created (human review required)</li>
     *   <li>AUTO_REJECT → no account created, rejection notification sent</li>
     * </ul>
     *
     * @param customerId   the customer UUID (as a String per the API contract)
     * @param customerEmail customer e-mail address
     * @param disposition  risk disposition: AUTO_APPROVE | MANUAL_REVIEW | AUTO_REJECT
     * @param score        numeric risk score
     */
    public Response sendRiskAssessedEvent(String customerId,
                                          String customerEmail,
                                          String disposition,
                                          int score) {
        return given()
                .spec(spec)
                .body(java.util.Map.of(
                        "customerId",    customerId,
                        "customerEmail", customerEmail,
                        "disposition",   disposition,
                        "score",         score
                ))
                .when()
                .post("/api/internal/events/risk-assessed");
    }

    // ─── Account Query ────────────────────────────────────────────────────────

    /**
     * Queries the provisioned account for the given customer.
     * Maps to: GET /api/v1/accounts/customer/{customerId}  → HTTP 200 OK with account details.
     */
    public Response getAccountByCustomerId(String customerId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/v1/accounts/customer/{customerId}", customerId);
    }
}
