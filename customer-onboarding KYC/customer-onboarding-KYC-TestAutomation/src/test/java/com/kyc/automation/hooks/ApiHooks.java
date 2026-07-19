package com.kyc.automation.hooks;

import com.kyc.automation.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber lifecycle hooks for API-level setup and teardown.
 *
 * <p>There is NO WebDriver, browser session, or screenshot logic here.
 * All diagnostic output on failure is limited to HTTP request/response
 * payload logging, which is captured automatically by RestAssured's
 * {@code enableLoggingOfRequestAndResponseIfValidationFails()} setting
 * in {@link com.kyc.automation.config.BaseApiConfig}.
 *
 * <p>The {@link ScenarioContext} is injected via PicoContainer (constructor DI)
 * and is reset before each scenario.
 */
public class ApiHooks {

    private static final Logger LOG = LoggerFactory.getLogger(ApiHooks.class);

    private final ScenarioContext scenarioContext;

    public ApiHooks(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Before Scenario
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Runs before every scenario.
     * Resets the shared scenario context and logs the scenario name for traceability.
     */
    @Before(order = 0)
    public void setUpScenario(Scenario scenario) {
        LOG.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LOG.info("▶  SCENARIO START: {}", scenario.getName());
        LOG.info("   Tags: {}", scenario.getSourceTagNames());
        scenarioContext.reset();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // After Scenario
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Runs after every scenario.
     * On failure: attaches last API response details to the Cucumber report for diagnostics.
     * Does NOT capture screenshots — this is a backend-only suite.
     */
    @After(order = 0)
    public void tearDownScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            LOG.error("✘  SCENARIO FAILED: {}", scenario.getName());
            attachLastResponseOnFailure(scenario);
        } else {
            LOG.info("✔  SCENARIO PASSED: {}", scenario.getName());
        }
        LOG.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // After each step
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attaches API response JSON to the Cucumber report after every step that
     * stored a response in the context — useful for debugging step-level failures.
     */
    @AfterStep
    public void logStepResponse(Scenario scenario) {
        Response lastResponse = scenarioContext.getLastResponse();
        if (lastResponse != null) {
            String logEntry = String.format(
                    "[HTTP %d] %s",
                    lastResponse.getStatusCode(),
                    safeBody(lastResponse)
            );
            LOG.debug("  Step response: {}", logEntry);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void attachLastResponseOnFailure(Scenario scenario) {
        Response lastResponse = scenarioContext.getLastResponse();
        if (lastResponse != null) {
            String attachment = String.format(
                    "=== LAST API RESPONSE ON FAILURE ===\n" +
                    "Status : %d\n" +
                    "Headers: %s\n" +
                    "Body   : %s\n",
                    lastResponse.getStatusCode(),
                    lastResponse.getHeaders(),
                    safeBody(lastResponse)
            );
            scenario.attach(attachment.getBytes(), "text/plain", "Last API Response");
            LOG.error(attachment);
        } else {
            LOG.warn("No API response captured in ScenarioContext for failed scenario: {}",
                     scenario.getName());
        }
    }

    private String safeBody(Response response) {
        try {
            String body = response.getBody().asString();
            return (body == null || body.isBlank()) ? "<empty body>" : body;
        } catch (Exception e) {
            return "<could not read body: " + e.getMessage() + ">";
        }
    }
}
