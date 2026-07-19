package com.kyc.automation.steps;

import com.kyc.automation.client.DocumentUploadApiClient;
import com.kyc.automation.client.RegistrationApiClient;
import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.context.ScenarioContext;
import io.cucumber.java.en.And;
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
 * Step definitions for the Document Upload feature.
 *
 * <p>All file uploads are performed via multipart/form-data using
 * {@link DocumentUploadApiClient}. No browser or WebDriver is involved.
 */
public class DocumentUploadSteps {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentUploadSteps.class);

    private final DocumentUploadApiClient documentClient;
    private final RegistrationApiClient   registrationClient;
    private final ScenarioContext         scenarioContext;

    private Response lastUploadResponse;
    private Response lastMetadataResponse;

    public DocumentUploadSteps(ScenarioContext scenarioContext) {
        this.scenarioContext    = scenarioContext;
        this.documentClient     = new DocumentUploadApiClient();
        this.registrationClient = new RegistrationApiClient();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the Document Service is running on its configured port")
    public void theDocumentServiceIsRunning() {
        int status = given()
                .spec(BaseApiConfig.documentServiceSpec())
                .when()
                .get("/api/documents/health-probe")
                .then()
                .extract()
                .statusCode();
        assertThat(status).isLessThan(500);
        LOG.info("Document Service health check passed (HTTP {})", status);
    }

    @Given("a registered customer exists for document upload tests")
    public void aRegisteredCustomerExistsForDocumentUploadTests() {
        String email = "doc.upload." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Map<String, Object> payload = RegistrationApiClient.validPayload(email);
        Response regResponse = registrationClient.registerCustomerRaw(payload);
        assertThat(regResponse.getStatusCode()).as("Customer registration for doc upload pre-condition").isEqualTo(201);

        String customerId = regResponse.jsonPath().getString("customerId");
        scenarioContext.set(ScenarioContext.CUSTOMER_ID, customerId);
        scenarioContext.set(ScenarioContext.CUSTOMER_EMAIL, email);
        scenarioContext.setLastResponse(regResponse);
        LOG.info("Pre-condition: registered customer customerId={}", customerId);
    }

    @Given("a valid PDF has already been uploaded for the registered customer")
    public void aValidPdfHasAlreadyBeenUploadedForTheRegisteredCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        assertThat(customerId).isNotBlank();

        Response uploadResponse = documentClient.uploadDocumentBytes(
                customerId,
                DocumentUploadApiClient.minimalPdfContent(),
                "kyc-document.pdf",
                "application/pdf"
        );
        assertThat(uploadResponse.getStatusCode()).as("Pre-upload should return 201").isEqualTo(201);
        String documentId = uploadResponse.jsonPath().getString("documentId");
        scenarioContext.set(ScenarioContext.DOCUMENT_ID, documentId);
        scenarioContext.setLastResponse(uploadResponse);
        LOG.info("Pre-condition: uploaded documentId={} for customerId={}", documentId, customerId);
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("a valid PDF file is uploaded for the registered customer")
    public void aValidPdfFileIsUploadedForTheRegisteredCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        Response response = documentClient.uploadDocumentBytes(
                customerId,
                DocumentUploadApiClient.minimalPdfContent(),
                "passport.pdf",
                "application/pdf"
        );
        lastUploadResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("PDF upload for customerId={} → HTTP {}", customerId, response.getStatusCode());
    }

    @When("a valid JPEG file is uploaded for the registered customer")
    public void aValidJpegFileIsUploadedForTheRegisteredCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        Response response = documentClient.uploadDocumentBytes(
                customerId,
                DocumentUploadApiClient.minimalPngContent(),
                "id-card.jpg",
                "image/jpeg"
        );
        lastUploadResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("JPEG upload for customerId={} → HTTP {}", customerId, response.getStatusCode());
    }

    @When("a file with an unsupported MIME type {string} is uploaded for the registered customer")
    public void aFileWithUnsupportedMimeTypeIsUploadedForTheRegisteredCustomer(String mimeType) {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        byte[] content = "not a valid KYC document".getBytes();
        Response response = documentClient.uploadDocumentBytes(customerId, content, "malware.exe", mimeType);
        lastUploadResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Unsupported MIME type upload ({}): customerId={} → HTTP {}", mimeType, customerId, response.getStatusCode());
    }

    @When("an oversized file larger than 5 MB is uploaded for the registered customer")
    public void anOversizedFileIsUploadedForTheRegisteredCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        Response response = documentClient.uploadDocumentBytes(
                customerId,
                DocumentUploadApiClient.oversizedContent(),
                "oversized.pdf",
                "application/pdf"
        );
        lastUploadResponse = response;
        scenarioContext.setLastResponse(response);
        LOG.info("Oversized upload for customerId={} → HTTP {}", customerId, response.getStatusCode());
    }

    @When("the latest document metadata is queried for the registered customer")
    public void theLatestDocumentMetadataIsQueriedForTheRegisteredCustomer() {
        String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        Response response = documentClient.getLatestDocumentMetadataByCustomer(customerId);
        lastMetadataResponse = response;
        scenarioContext.setLastResponse(response);
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the document upload response status code is {int}")
    public void theDocumentUploadResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastUploadResponse.getStatusCode())
                .as("Document upload HTTP status")
                .isEqualTo(expectedStatus);
    }

    @Then("the upload response contains a document ID")
    public void theUploadResponseContainsADocumentId() {
        String documentId = lastUploadResponse.jsonPath().getString("documentId");
        assertThat(documentId)
                .as("Upload response should contain a non-blank documentId")
                .isNotBlank();
        scenarioContext.set(ScenarioContext.DOCUMENT_ID, documentId);
        LOG.info("Stored documentId={} in ScenarioContext", documentId);
    }

    @Then("the upload response contains the customer ID")
    public void theUploadResponseContainsTheCustomerId() {
        String expectedCustomerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
        String actualCustomerId   = lastUploadResponse.jsonPath().getString("customerId");
        assertThat(actualCustomerId)
                .as("Upload response customerId should match the registered customer")
                .isEqualTo(expectedCustomerId);
    }

    @Then("the metadata response status code is {int}")
    public void theMetadataResponseStatusCodeIs(int expectedStatus) {
        assertThat(lastMetadataResponse.getStatusCode())
                .as("Metadata query HTTP status")
                .isEqualTo(expectedStatus);
    }

    @Then("the metadata contains a non-null document ID")
    public void theMetadataContainsANonNullDocumentId() {
        String documentId = lastMetadataResponse.jsonPath().getString("documentId");
        assertThat(documentId).as("Metadata should contain a non-null documentId").isNotBlank();
    }

    @Then("the metadata content type is {string}")
    public void theMetadataContentTypeIs(String expectedContentType) {
        String actual = lastMetadataResponse.jsonPath().getString("contentType");
        assertThat(actual)
                .as("Metadata contentType")
                .isEqualTo(expectedContentType);
    }
}
