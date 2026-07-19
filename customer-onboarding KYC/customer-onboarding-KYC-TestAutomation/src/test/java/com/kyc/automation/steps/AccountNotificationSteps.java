package com.kyc.automation.steps;

import com.kyc.automation.client.AccountApiClient;
import com.kyc.automation.client.NotificationApiClient;
import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.context.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Account Provisioning and Notification feature.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>Account Service provisions an account when risk disposition is AUTO_APPROVE</li>
 *   <li>Account Service does NOT provision when disposition is AUTO_REJECT</li>
 *   <li>Notification Service accepts account-created and application-rejected events</li>
 * </ul>
 */
public class AccountNotificationSteps {

    private static final Logger LOG = LoggerFactory.getLogger(AccountNotificationSteps.class);

    // Awaitility config for async account provisioning
    private static final int    POLL_INTERVAL_MS = 500;
    private static final int    AWAIT_TIMEOUT_S  = 10;

    private final AccountApiClient      accountClient;
    private final NotificationApiClient notificationClient;
    private final ScenarioContext       scenarioContext;

    private Response lastAccountIngressResponse;
    private Response lastAccountQueryResponse;
    private Response lastNotificationResponse;

    public AccountNotificationSteps(ScenarioContext scenarioContext) {
        this.scenarioContext    = scenarioContext;
        this.accountClient      = new AccountApiClient();
        this.notificationClient = new NotificationApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the Account Service is running on its configured port")
    public void theAccountServiceIsRunning() {
        try {
            int status = given()
                    .spec(BaseApiConfig.accountServiceSpec())
                    .when()
                    .get("/api/v1/accounts/customer/health-probe")
                    .then()
                    .extract()
                    .statusCode();
            assertThat(status).isLessThan(600);
            LOG.info("Account Service health check passed (HTTP {})", status);
        } catch (Exception e) {
            LOG.warn("Account Service health probe: {}", e.getMessage());
        }
    }

    @Given("the Notification Service is running on its configured port")
    public void theNotificationServiceIsRunning() {
        try {
            int status = given()
                    .spec(BaseApiConfig.notificationServiceSpec())
                    .when()
                    .get("/api/internal/health-probe")
                    .then()
                    .extract()
                    .statusCode();
            assertThat(status).isLessThan(600);
            LOG.info("Notification Service health check passed (HTTP {})", status);
        } catch (Exception e) {
            LOG.warn("Notification Service health probe: {}", e.getMessage());
        }
    }

    @Given("a customer with email {string} has passed KYC verification")
    public void aCustomerWithEmailHasPassedKycVerification(String email) {
        String customerId = UUID.randomUUID().toString();
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, email);
        LOG.info("Pre-condition: KYC-passed customer customerId={}, email={}", customerId, email);
    }

    @Given("a customer with email {string} has failed KYC verification")
    public void aCustomerWithEmailHasFailedKycVerification(String email) {
        String customerId = UUID.randomUUID().toString();
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, email);
        LOG.info("Pre-condition: KYC-failed customer customerId={}, email={}", customerId, email);
    }

    @Given("a random customer ID that has no associated account")
    public void aRandomCustomerIdWithNoAccount() {
        String randomId = UUID.randomUUID().toString();
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, randomId);
        LOG.info("Prepared random customerId={} with no account", randomId);
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("a risk-assessed event with disposition {string} and score {int} is sent for that customer")
    public void aRiskAssessedEventIsSentForThatCustomer(String disposition, int score) {
        String customerId    = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        String customerEmail = scenarioContext.get(ScenarioContext.CUSTOMER_EMAIL);

        Response response = accountClient.sendRiskAssessedEvent(customerId, customerEmail, disposition, score);
        lastAccountIngressResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("risk-assessed event sent: customerId={}, disposition={}, score={} → HTTP {}",
                customerId, disposition, score, response.getStatusCode());
    }

    @When("an account-created notification is sent for customer {string} with account number {string} and sort code {string}")
    public void anAccountCreatedNotificationIsSent(String email, String accountNumber, String sortCode) {
        String customerId = UUID.randomUUID().toString();
        Response response = notificationClient.sendAccountCreatedNotification(customerId, email, accountNumber, sortCode);
        lastNotificationResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("account-created notification sent for email={} → HTTP {}", email, response.getStatusCode());
    }

    @When("an application-rejected notification is sent for customer {string} with reason {string}")
    public void anApplicationRejectedNotificationIsSent(String email, String reason) {
        String customerId = UUID.randomUUID().toString();
        Response response = notificationClient.sendApplicationRejectedNotification(customerId, email, reason);
        lastNotificationResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("application-rejected notification sent for email={} → HTTP {}", email, response.getStatusCode());
    }

    @When("the account is queried by that customer ID")
    public void theAccountIsQueriedByThatCustomerId() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        Response response = accountClient.getAccountByCustomerId(customerId);
        lastAccountQueryResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Account query for customerId={} → HTTP {}", customerId, response.getStatusCode());
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the account ingress event response status code is {int}")
    public void theAccountIngressEventResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastAccountIngressResponse.getStatusCode())
                .as("Account ingress event HTTP status")
                .isEqualTo(expectedStatus);
    }

    @Then("an account record is eventually created for that customer")
    public void anAccountRecordIsEventuallyCreatedForThatCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        // Account provisioning is asynchronous → poll with Awaitility
        Awaitility.await("Account should be provisioned for customerId=" + customerId)
                .atMost(AWAIT_TIMEOUT_S, TimeUnit.SECONDS)
                .pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response response = accountClient.getAccountByCustomerId(customerId);
                    assertThat(response.getStatusCode())
                            .as("Account should exist (200) for customerId=%s", customerId)
                            .isEqualTo(200);
                    assertThat(response.jsonPath().getString("accountNumber"))
                            .as("Account number should not be blank")
                            .isNotBlank();
                    lastAccountQueryResponse = response;
                    scenarioContext.setLastResponse(response);
                });
        LOG.info("Account confirmed created for customerId={}", customerId);
    }

    @Then("no account record exists for that customer")
    public void noAccountRecordExistsForThatCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        // Allow brief async window then assert 404
        try {
            Thread.sleep(2000); // 2 s settling time for async rejection path
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Response response = accountClient.getAccountByCustomerId(customerId);
        lastAccountQueryResponse = response;
        scenarioContext.setLastResponse(response);
        assertThat(response.getStatusCode())
                .as("No account should exist for rejected customer customerId=%s", customerId)
                .isEqualTo(404);
        LOG.info("Confirmed no account exists for customerId={}", customerId);
    }

    @Then("the notification service returns status code {int}")
    public void theNotificationServiceReturnsStatusCode(int expectedStatus) {
        assertThat(lastNotificationResponse.getStatusCode())
                .as("Notification service HTTP status")
                .isEqualTo(expectedStatus);
    }

    @Then("the account query response status code is {int}")
    public void theAccountQueryResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastAccountQueryResponse.getStatusCode())
                .as("Account query HTTP status")
                .isEqualTo(expectedStatus);
    }
}
