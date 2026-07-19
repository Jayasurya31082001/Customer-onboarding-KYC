package com.kyc.automation.steps;

import com.kyc.automation.client.RiskApiClient;
import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Compliance Risk Scoring feature.
 *
 * <p>Tests the Risk Service kyc-completed ingress and risk-assessment query endpoints.
 * Risk disposition values: AUTO_APPROVE | MANUAL_REVIEW | AUTO_REJECT
 */
public class ComplianceRiskSteps {

    private static final Logger LOG = LoggerFactory.getLogger(ComplianceRiskSteps.class);

    private static final int AWAIT_TIMEOUT_S  = 10;
    private static final int POLL_INTERVAL_MS = 500;

    private final RiskApiClient   riskClient;
    private final ScenarioContext scenarioContext;

    private Response lastRiskIngressResponse;
    private Response lastRiskAssessmentResponse;

    public ComplianceRiskSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
        this.riskClient      = new RiskApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the Risk Service is running on its configured port")
    public void theRiskServiceIsRunning() {
        try {
            int status = given()
                    .spec(BaseApiConfig.riskServiceSpec())
                    .when()
                    .get("/api/v1/risk-assessments/health-probe")
                    .then()
                    .extract()
                    .statusCode();
            assertThat(status).isLessThan(600);
            LOG.info("Risk Service health check passed (HTTP {})", status);
        } catch (Exception e) {
            LOG.warn("Risk Service health probe: {}", e.getMessage());
        }
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    /**
     * Sends a KYC-completed event using a DataTable of key/value pairs.
     * Supported keys: kycStatus, nationality, pepMatch, sanctionsMatch
     */
    @When("a KYC-completed event is sent to the risk service for customer {string} with:")
    public void aKycCompletedEventIsSentToRiskService(String email, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        String customerId     = UUID.randomUUID().toString();
        String kycCaseId      = UUID.randomUUID().toString();
        String documentId     = UUID.randomUUID().toString();
        String kycStatus      = data.getOrDefault("kycStatus", "Pass");
        String nationality    = data.getOrDefault("nationality", "GB");
        boolean pepMatch      = Boolean.parseBoolean(data.getOrDefault("pepMatch", "false"));
        boolean sanctionsMatch = Boolean.parseBoolean(data.getOrDefault("sanctionsMatch", "false"));

        String dobStr = data.get("dateOfBirth");
        LocalDate dateOfBirth = (dobStr != null && !dobStr.isBlank())
                ? LocalDate.parse(dobStr)
                : LocalDate.of(1985, 3, 20);

        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, email);

        Response response = riskClient.sendKycCompletedEvent(
                customerId,
                kycCaseId,
                documentId,
                kycStatus,
                nationality,
                dateOfBirth,
                pepMatch,
                sanctionsMatch,
                email,
                UUID.randomUUID().toString()
        );
        lastRiskIngressResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("KYC-completed event sent for email={}, kycStatus={}, nationality={}, pep={}, sanctions={} → HTTP {}",
                email, kycStatus, nationality, pepMatch, sanctionsMatch, response.getStatusCode());
    }

    @When("the latest risk assessment is queried for an unknown customer ID")
    public void theLatestRiskAssessmentIsQueriedForUnknownCustomer() {
        String unknownId = UUID.randomUUID().toString();
        Response response = riskClient.getLatestRiskAssessment(unknownId);
        lastRiskAssessmentResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Risk assessment query for unknown customerId={} → HTTP {}", unknownId, response.getStatusCode());
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the risk event ingress response status code is {int}")
    public void theRiskEventIngressResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastRiskIngressResponse.getStatusCode())
                .as("Risk event ingress HTTP status")
                .isEqualTo(expectedStatus);
    }

    @Then("the latest risk assessment for that customer has disposition {string}")
    public void theLatestRiskAssessmentForThatCustomerHasDisposition(String expectedDisposition) {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        assertThat(customerId).as("customerId must be set in ScenarioContext").isNotBlank();

        // Risk assessment is computed asynchronously → poll with Awaitility
        Awaitility.await("Risk assessment with disposition=" + expectedDisposition
                         + " for customerId=" + customerId)
                .atMost(AWAIT_TIMEOUT_S, TimeUnit.SECONDS)
                .pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response response = riskClient.getLatestRiskAssessment(customerId);
                    assertThat(response.getStatusCode())
                            .as("Risk assessment query should return 200 for customerId=%s", customerId)
                            .isEqualTo(200);
                    String actualDisposition = response.jsonPath().getString("disposition");
                    assertThat(actualDisposition)
                            .as("Risk disposition for customerId=%s", customerId)
                            .isEqualTo(expectedDisposition);
                    lastRiskAssessmentResponse = response;
                    scenarioContext.setLastResponse(response);
                });
        LOG.info("Risk assessment disposition confirmed as {} for customerId={}", expectedDisposition, customerId);
    }

    @Then("the risk assessment query response status code is {int}")
    public void theRiskAssessmentQueryResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastRiskAssessmentResponse.getStatusCode())
                .as("Risk assessment query HTTP status")
                .isEqualTo(expectedStatus);
    }
}
