package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.util.ApiClientUtil;
import com.kyc.automation.util.ValidationUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
        ValidationUtil.requireNonEmpty(firstName, "firstName", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(lastName, "lastName", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(email, "email", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonNull(dateOfBirth, "dateOfBirth", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(phoneNumber, "phoneNumber", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(nationality, "nationality", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(addressLine1, "addressLine1", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(city, "city", "RegistrationApiClient.registerCustomer");
        ValidationUtil.requireNonEmpty(postcode, "postcode", "RegistrationApiClient.registerCustomer");

        Map<String, Object> body = new HashMap<>();
        body.put("firstName",    firstName);
        body.put("lastName",     lastName);
        body.put("email",        email);
        body.put("dateOfBirth",  dateOfBirth.toString());
        body.put("phoneNumber",  phoneNumber);
        body.put("nationality",  nationality);
        body.put("addressLine1", addressLine1);
        body.put("city",         city);
        body.put("postcode",     postcode);

        return ApiClientUtil.executePost(spec, "/api/v1/customers", body);
    }

    /**
     * Registers a customer using a pre-built body map (useful for partial/invalid payloads).
     */
    public Response registerCustomerRaw(Map<String, Object> body) {
        ValidationUtil.requireNonEmpty(body, "body", "RegistrationApiClient.registerCustomerRaw");

        return ApiClientUtil.executePost(spec, "/api/v1/customers", body);
    }

    /**
     * Fetches a single customer by UUID.
     */
    public Response getCustomer(String customerId) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "RegistrationApiClient.getCustomer");

        return ApiClientUtil.executeGet(spec, "/api/v1/customers/{customerId}", customerId);
    }

    /**
     * Queries customers filtered by onboarding status.
     */
    public Response getCustomersByStatus(String status) {
        ValidationUtil.requireNonEmpty(status, "status", "RegistrationApiClient.getCustomersByStatus");

        return ApiClientUtil.executeGetWithQueryParam(spec, "/api/v1/customers", "status", status);
    }

    /**
     * Updates a customer's onboarding status via PATCH.
     */
    public Response updateOnboardingStatus(String customerId, String newStatus) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "RegistrationApiClient.updateOnboardingStatus");
        ValidationUtil.requireNonEmpty(newStatus, "newStatus", "RegistrationApiClient.updateOnboardingStatus");

        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        return ApiClientUtil.executePatch(spec, "/api/v1/customers/{customerId}/status", body, customerId);
    }

    /**
     * Convenience factory: builds a minimal valid registration payload
     * using sensible defaults so callers need only override the field under test.
     */
    public static Map<String, Object> validPayload(String uniqueEmail) {
        ValidationUtil.requireNonEmpty(uniqueEmail, "uniqueEmail", "RegistrationApiClient.validPayload");

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
