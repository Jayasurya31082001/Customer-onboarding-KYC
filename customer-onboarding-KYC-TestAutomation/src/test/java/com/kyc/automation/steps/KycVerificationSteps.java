package com.kyc.automation.steps;

import com.kyc.automation.client.KycVerificationApiClient;
import com.kyc.automation.client.RegistrationApiClient;
import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the KYC Verification feature.
 *
 * <p>KYC verification is event-driven. These steps test the KYC Service
 * ingress endpoints directly via RestAssured (no browser involved).
 */
public class KycVerificationSteps {

    private static final Logger LOG = LoggerFactory.getLogger(KycVerificationSteps.class);

    private final KycVerificationApiClient kycClient;
    private final RegistrationApiClient    registrationClient;
    private final ScenarioContext          scenarioContext;

    private Response lastKycResponse;

    public KycVerificationSteps(ScenarioContext scenarioContext) {
        this.scenarioContext    = scenarioContext;
        this.kycClient          = new KycVerificationApiClient();
        this.registrationClient = new RegistrationApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the KYC Service is running on its configured port")
    public void theKycServiceIsRunning() {
        assertThat(isPortOpen(BaseApiConfig.KYC_SERVICE_PORT))
                .as("KYC Service must be running on port %d", BaseApiConfig.KYC_SERVICE_PORT)
                .isTrue();
        LOG.info("KYC Service health check passed on port {}", BaseApiConfig.KYC_SERVICE_PORT);
    }

    private static boolean isPortOpen(int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", port), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Given("a valid customer ID exists for KYC initiation")
    public void aValidCustomerIdExistsForKycInitiation() {
        String customerId = UUID.randomUUID().toString();
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        LOG.info("Prepared customerId={} for KYC initiation", customerId);
    }

    @Given("a valid customer ID and document ID exist for KYC verification")
    public void aValidCustomerIdAndDocumentIdExistForKycVerification() {
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, UUID.randomUUID().toString());
        scenarioContext.set(ScenarioContext.DOCUMENT_ID, UUID.randomUUID().toString());
    }

    @Given("a customer-registered event has been accepted for a clean customer profile")
    public void aCustomerRegisteredEventHasBeenAcceptedForCleanProfile() {
        String email = "clean.kyc." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Response regResponse = registrationClient.registerCustomerRaw(RegistrationApiClient.validPayload(email));
        String customerId = regResponse.jsonPath().getString("customerId");
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);

        Response response = kycClient.sendCustomerRegisteredEvent(customerId, UUID.randomUUID().toString());
        assertThat(response.getStatusCode())
                .as("Customer-registered event for clean profile should return 202")
                .isEqualTo(202);
        scenarioContext.setLastResponse(response);
        LOG.info("Customer-registered event accepted for clean profile customerId={}", customerId);
    }

    @Given("a customer-registered event has been accepted for a high-risk customer profile")
    public void aCustomerRegisteredEventHasBeenAcceptedForHighRiskProfile() {
        String email = "high.risk.kyc." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Map<String, Object> payload = RegistrationApiClient.validPayload(email);
        payload.put("nationality", "IR");
        Response regResponse = registrationClient.registerCustomerRaw(payload);
        String customerId = regResponse.jsonPath().getString("customerId");
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);

        Response response = kycClient.sendCustomerRegisteredEvent(customerId, UUID.randomUUID().toString());
        assertThat(response.getStatusCode())
                .as("Customer-registered event for high-risk profile should return 202")
                .isEqualTo(202);
        scenarioContext.setLastResponse(response);
        LOG.info("Customer-registered event accepted for high-risk profile customerId={}", customerId);
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("the customer-registered event is sent to the KYC service")
    public void theCustomerRegisteredEventIsSentToTheKycService() {
        String customerId    = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        String correlationId = UUID.randomUUID().toString();

        Response response = kycClient.sendCustomerRegisteredEvent(customerId, correlationId);
        lastKycResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("customer-registered event sent for customerId={} → HTTP {}", customerId, response.getStatusCode());
    }

    @When("the document-uploaded event is sent to the KYC service")
    public void theDocumentUploadedEventIsSentToTheKycService() {
        String customerId  = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        String documentId  = scenarioContext.get(ScenarioContext.DOCUMENT_ID);

        if (customerId == null || customerId.isBlank()) {
            customerId = UUID.randomUUID().toString();
            scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        }
        if (documentId == null || documentId.isBlank()) {
            documentId = UUID.randomUUID().toString();
            scenarioContext.set(ScenarioContext.DOCUMENT_ID, documentId);
        }

        Response response = kycClient.sendDocumentUploadedEvent(customerId, documentId, UUID.randomUUID().toString());
        lastKycResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("document-uploaded event sent for customerId={}, documentId={} → HTTP {}",
                customerId, documentId, response.getStatusCode());
    }

    @When("the document-uploaded event is sent with a new customer ID and document ID")
    public void theDocumentUploadedEventIsSentWithNewIds() {
        String customerId  = UUID.randomUUID().toString();
        String documentId  = UUID.randomUUID().toString();
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.DOCUMENT_ID, documentId);

        Response response = kycClient.sendDocumentUploadedEvent(customerId, documentId, UUID.randomUUID().toString());
        lastKycResponse = response;
        scenarioContext.setLastResponse(response);
    }

    @When("an invalid customer-registered event with missing fields is sent")
    public void anInvalidCustomerRegisteredEventWithMissingFieldsIsSent() {
        Response response = kycClient.sendInvalidCustomerRegisteredEvent();
        lastKycResponse = response;
        scenarioContext.setLastResponse(response);
    }

    @When("the document-uploaded event is sent to complete KYC verification")
    public void theDocumentUploadedEventIsSentToCompleteKycVerification() {
        theDocumentUploadedEventIsSentToTheKycService();
    }

    @When("the document-uploaded event is sent to trigger KYC for the high-risk customer")
    public void theDocumentUploadedEventIsSentForHighRiskCustomer() {
        theDocumentUploadedEventIsSentToTheKycService();
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the KYC ingress response status code is {int}")
    public void theKycIngressResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastKycResponse.getStatusCode())
                .as("KYC ingress HTTP status code")
                .isEqualTo(expectedStatus);
    }
}
