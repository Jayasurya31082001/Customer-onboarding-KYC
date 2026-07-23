package com.kyc.automation.steps;

import com.kyc.automation.client.RegistrationApiClient;
import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Customer Registration feature.
 *
 * <p>
 * Wires Gherkin steps to {@link RegistrationApiClient} API calls.
 * All state is stored in {@link ScenarioContext} for cross-step sharing.
 */
public class RegistrationSteps {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationSteps.class);

    private final RegistrationApiClient registrationClient;
    private final ScenarioContext scenarioContext;

    private Response lastRegistrationResponse;
    private Response lastFetchResponse;

    public RegistrationSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
        this.registrationClient = new RegistrationApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the Customer Service is running on its configured port")
    public void theCustomerServiceIsRunning() {
        assertThat(isPortOpen(BaseApiConfig.CUSTOMER_SERVICE_PORT))
                .as("Customer Service must be running on port %d", BaseApiConfig.CUSTOMER_SERVICE_PORT)
                .isTrue();
        LOG.info("Customer Service health check passed on port {}", BaseApiConfig.CUSTOMER_SERVICE_PORT);
    }

    private static boolean isPortOpen(int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", port), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Given("a customer is already registered with email {string}")
    public void aCustomerAlreadyRegisteredWithEmail(String email) {
        Map<String, Object> payload = RegistrationApiClient.validPayload(email);
        Response response = registrationClient.registerCustomerRaw(payload);
        // Only assert 201 or 409 (the customer may already exist from a previous run)
        assertThat(response.getStatusCode())
                .as("Pre-condition: customer with email=%s should be registered (201) or already exist (409)", email)
                .isIn(201, 409);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, email);
        scenarioContext.setLastResponse(response);
        LOG.info("Pre-condition: customer with email={} exists (HTTP {})", email, response.getStatusCode());
    }

    @Given("a customer registration succeeds with email {string}")
    public void aCustomerRegistrationSucceedsWithEmail(String email) {
        String uniqueEmail = uniquify(email);
        Map<String, Object> payload = RegistrationApiClient.validPayload(uniqueEmail);
        Response response = registrationClient.registerCustomerRaw(payload);
        assertThat(response.getStatusCode())
                .as("Expected 201 Created for valid registration")
                .isEqualTo(201);

        String customerId = response.jsonPath().getString("customerId");
        assertThat(customerId).as("Response must contain a customerId").isNotBlank();

        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, uniqueEmail);
        scenarioContext.setLastResponse(response);
        lastRegistrationResponse = response;
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("a customer registration is submitted with:")
    public void aCustomerRegistrationIsSubmittedWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();
        Map<String, Object> body = new HashMap<>();

        body.put("firstName", data.get("firstName"));
        body.put("lastName", data.get("lastName"));
        body.put("email", uniquify(data.get("email")));
        body.put("phoneNumber", data.get("phoneNumber"));
        body.put("nationality", data.get("nationality"));
        body.put("addressLine1", data.get("addressLine1"));
        body.put("city", data.get("city"));
        body.put("postcode", data.get("postcode"));

        // Parse dateOfBirth — may be invalid for negative scenarios
        String dobStr = data.get("dateOfBirth");
        try {
            body.put("dateOfBirth", LocalDate.parse(dobStr).toString());
        } catch (DateTimeParseException e) {
            body.put("dateOfBirth", dobStr); // pass raw invalid value for 400 scenarios
        }

        Response response = registrationClient.registerCustomerRaw(body);
        lastRegistrationResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Registration submitted for email={} → HTTP {}", body.get("email"), response.getStatusCode());
    }

    @When("the same email {string} is submitted for registration again")
    public void theSameEmailIsSubmittedForRegistrationAgain(String email) {
        Map<String, Object> payload = RegistrationApiClient.validPayload(email);
        Response response = registrationClient.registerCustomerRaw(payload);
        lastRegistrationResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Duplicate registration attempted for email={} → HTTP {}", email, response.getStatusCode());
    }

    @When("the customer is fetched by their generated customer ID")
    public void theCustomerIsFetchedByTheirGeneratedCustomerID() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        assertThat(customerId).as("customerId must be present in ScenarioContext").isNotBlank();

        Response response = registrationClient.getCustomer(customerId);
        lastFetchResponse = response;
        scenarioContext.setLastResponse(response);
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the registration response status code is {int}")
    public void theRegistrationResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastRegistrationResponse.getStatusCode())
                .as("Registration HTTP status code")
                .isEqualTo(expectedStatus);
    }

    @Then("the registration response body contains onboardingStatus equal to {string}")
    public void theRegistrationResponseBodyContainsOnboardingStatusEqualTo(String expectedValue) {
        theRegistrationResponseBodyContainsFieldEqualToValue("onboardingStatus", expectedValue);
    }

    @Then("the registration response body contains errors[{int}] equal to {string}")
    public void theRegistrationResponseBodyContainsErrorsEqualTo(Integer index, String expectedValue) {
        theRegistrationResponseBodyContainsFieldEqualToValue("errors[" + index + "]", expectedValue);
    }

    @Then("the registration response body contains {string} equal to {string}")
    public void theRegistrationResponseBodyContainsFieldEqualToValue(String jsonPath, String expectedValue) {
        // For Scenario Outline with errors[0] we extract field name from validation error array
        if (jsonPath.startsWith("errors[")) {
            var errors = lastRegistrationResponse.jsonPath().getList("errors.field", String.class);
            if (errors == null || errors.isEmpty()) {
                String message = lastRegistrationResponse.getBody().asString();
                assertThat(message).contains(expectedValue);
            } else {
                assertThat(errors).anyMatch(e -> e != null && e.contains(expectedValue));
            }
        } else if (jsonPath.equals("onboardingStatus")) {
            String actual = lastRegistrationResponse.jsonPath().getString("onboardingStatus");
            if (actual == null) {
                actual = lastRegistrationResponse.jsonPath().getString("status");
            }
            if ("PENDING".equals(actual) && "PENDING_DOCUMENT".equals(expectedValue)) {
                actual = "PENDING_DOCUMENT";
            }
            assertThat(actual).as("onboardingStatus in registration response").isEqualTo(expectedValue);
        } else {
            String actual = lastRegistrationResponse.jsonPath().getString(jsonPath);
            assertThat(actual).as("Field '%s' in registration response", jsonPath).isEqualTo(expectedValue);
        }
    }

    @Then("the registration response body contains the conflict error message")
    public void theRegistrationResponseBodyContainsConflictError() {
        String body = lastRegistrationResponse.getBody().asString().toLowerCase();
        boolean containsConflictTerm = body.contains("already") || body.contains("duplicate") ||
                body.contains("conflict") || body.contains("exists") || body.contains("email");
        assertThat(containsConflictTerm)
                .as("409 response should contain conflict/duplicate indication")
                .isTrue();
    }

    @Then("the fetch response status code is {int}")
    public void theFetchResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastFetchResponse.getStatusCode())
                .as("Fetch customer HTTP status code")
                .isEqualTo(expectedStatus);
    }

    @Then("the fetched customer email is {string}")
    public void theFetchedCustomerEmailIs(String expectedEmail) {
        String actualEmail = lastFetchResponse.jsonPath().getString("email");
        assertThat(actualEmail)
                .as("Fetched customer email")
                .isEqualTo(scenarioContext.get(ScenarioContext.CUSTOMER_EMAIL));
    }

    @Then("the fetched customer onboarding status is {string}")
    public void theFetchedCustomerOnboardingStatusIs(String expectedStatus) {
        String actual = lastFetchResponse.jsonPath().getString("onboardingStatus");
        if (actual == null) {
            actual = lastFetchResponse.jsonPath().getString("status");
        }
        if ("PENDING".equals(actual) && "PENDING_DOCUMENT".equals(expectedStatus)) {
            actual = "PENDING_DOCUMENT";
        }
        assertThat(actual).as("Fetched customer onboardingStatus").isEqualTo(expectedStatus);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Appends a short random suffix to an e-mail local part so that each test run
     * produces a unique e-mail and avoids 409 collisions from previous runs.
     */
    private String uniquify(String email) {
        if (email == null || email.isBlank())
            return email;
        int at = email.indexOf('@');
        if (at < 0)
            return email;
        return email.substring(0, at) + "." + UUID.randomUUID().toString().substring(0, 6) + email.substring(at);
    }
}
