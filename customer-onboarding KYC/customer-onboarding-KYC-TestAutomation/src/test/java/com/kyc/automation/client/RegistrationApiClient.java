package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API client for the Customer Service (port 8081).
 *
 * <p>Covers:
 * <ul>
 *   <li>POST /api/v1/customers              – register a new customer</li>
 *   <li>GET  /api/v1/customers/{customerId} – fetch a customer by ID</li>
 *   <li>GET  /api/v1/customers?status=...   – fetch customers by onboarding status</li>
 *   <li>PATCH /api/v1/customers/{id}/status – update a customer's onboarding status</li>
 * </ul>
 */
public class RegistrationApiClient {

    private final RequestSpecification spec;

    public RegistrationApiClient() {
        this.spec = BaseApiConfig.customerServiceSpec();
    }

    /**
     * Sends a full customer registration payload and returns the raw {@link Response}.
     * Callers assert on status code and body fields.
     */
    public Response registerCustomer(String firstName,
                                     String lastName,
                                     String email,
                                     LocalDate dateOfBirth,
                                     String phoneNumber,
                                     String nationality,
                                     String addressLine1,
                                     String city,
                                     String postcode) {

        Map<String, Object> body = new HashMap<>();
        body.put("firstName",   firstName);
        body.put("lastName",    lastName);
        body.put("email",       email);
        body.put("dateOfBirth", dateOfBirth != null ? dateOfBirth.toString() : null);
        body.put("phoneNumber", phoneNumber);
        body.put("nationality", nationality);
        body.put("addressLine1", addressLine1);
        body.put("city",        city);
        body.put("postcode",    postcode);

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/v1/customers");
    }

    /**
     * Registers a customer using a pre-built body map (useful for partial/invalid payloads).
     */
    public Response registerCustomerRaw(Map<String, Object> body) {
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/v1/customers");
    }

    /**
     * Fetches a single customer by UUID.
     */
    public Response getCustomer(String customerId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/v1/customers/{customerId}", customerId);
    }

    /**
     * Queries customers filtered by onboarding status.
     */
    public Response getCustomersByStatus(String status) {
        return given()
                .spec(spec)
                .queryParam("status", status)
                .when()
                .get("/api/v1/customers");
    }

    /**
     * Updates a customer's onboarding status via PATCH.
     */
    public Response updateOnboardingStatus(String customerId, String newStatus) {
        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        return given()
                .spec(spec)
                .body(body)
                .when()
                .patch("/api/v1/customers/{customerId}/status", customerId);
    }

    /**
     * Convenience factory: builds a minimal valid registration payload
     * using sensible defaults so callers need only override the field under test.
     */
    public static Map<String, Object> validPayload(String uniqueEmail) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName",    "Alice");
        body.put("lastName",     "Walker");
        body.put("email",        uniqueEmail);
        body.put("dateOfBirth",  "1990-06-15");
        body.put("phoneNumber",  "+447911123456");
        body.put("nationality",  "GB");
        body.put("addressLine1", "221B Baker Street");
        body.put("city",         "London");
        body.put("postcode",     "NW1 6XE");
        return body;
    }
}
