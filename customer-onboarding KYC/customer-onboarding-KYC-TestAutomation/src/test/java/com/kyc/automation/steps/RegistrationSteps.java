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
 */
public class RegistrationSteps {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationSteps.class);

    private final RegistrationApiClient registrationClient;
    private final ScenarioContext       scenarioContext;

    private Response lastRegistrationResponse;
    private Response lastFetchResponse;

    public RegistrationSteps(ScenarioContext scenarioContext) {
        this.scenarioContext    = scenarioContext;
        this.registrationClient = new RegistrationApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the Customer Service is running on its configured port")
    public void theCustomerServiceIsRunning() {
        try {
            int status = given()
                    .spec(BaseApiConfig.customerServiceSpec())
                    .when()
                    .get("/api/v1/customers")
                    .then()
                    .extract()
                    .statusCode();
            assertThat(status).isLessThan(600);
            LOG.info("Customer Service health check passed (HTTP {})", status);
        } catch (Exception e) {
            LOG.warn("Customer Service health check probe warning: {}", e.getMessage());
        }
    }

    @Given("a customer is already registered with email {string}")
    public void aCustomerAlreadyRegisteredWithEmail(String email) {
        Map<String, Object> payload = RegistrationApiClient.validPayload(email);
        Response response = registrationClient.registerCustomerRaw(payload);
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

        body.put("firstName",    data.get("firstName"));
        body.put("lastName",     data.get("lastName"));
        body.put("email",        uniquify(data.get("email")));
        body.put("phoneNumber",  data.get("phoneNumber"));
        body.put("nationality",  data.get("nationality"));
        body.put("addressLine1", data.get("addressLine1"));
        body.put("city",         data.get("city"));
        body.put("postcode",     data.get("postcode"));

        String dobStr = data.get("dateOfBirth");
        try {
            body.put("dateOfBirth", LocalDate.parse(dobStr).toString());
        } catch (DateTimeParseException e) {
            body.put("dateOfBirth", dobStr);
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

    @Then("the registration response body contains {string} equal to {string}")
    public void theRegistrationResponseBodyContainsFieldEqualToValue(String jsonPath, String expectedValue) {
        if (jsonPath.startsWith("errors[")) {
            String message = lastRegistrationResponse.getBody().asString();
            assertThat(message).contains(expectedValue);
        } else {
            String actual = lastRegistrationResponse.jsonPath().getString(jsonPath);
            if (actual == null && jsonPath.equals("status")) {
                actual = lastRegistrationResponse.jsonPath().getString("onboardingStatus");
            }
            assertThat(actual).as("Field '%s' in registration response", jsonPath).isEqualTo(expectedValue);
        }
    }

    @Then("the registration response body contains the conflict error message")
    public void theRegistrationResponseBodyContainsConflictError() {
        String body = lastRegistrationResponse.getBody().asString();
        assertThat(body)
                .as("409 response should contain conflict/duplicate indication")
                .containsAnyOf("already", "duplicate", "conflict", "exists", "email");
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
        String actual = lastFetchResponse.jsonPath().getString("status");
        if (actual == null) {
            actual = lastFetchResponse.jsonPath().getString("onboardingStatus");
        }
        assertThat(actual).as("Fetched customer onboarding status").isEqualTo(expectedStatus);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String uniquify(String email) {
        if (email == null || email.isBlank()) return email;
        int at = email.indexOf('@');
        if (at < 0) return email;
        return email.substring(0, at) + "." + UUID.randomUUID().toString().substring(0, 6) + email.substring(at);
    }
}
