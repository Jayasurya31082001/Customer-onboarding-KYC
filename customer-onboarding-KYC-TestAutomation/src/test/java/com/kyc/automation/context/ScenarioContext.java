package com.kyc.automation.context;

import com.kyc.automation.util.ValidationUtil;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local scenario context shared across step-definition classes via PicoContainer DI.
 *
 * <p>Stores transient state for the duration of a single Cucumber scenario.
 * The {@link com.kyc.automation.hooks.ApiHooks} hook calls {@link #reset()} before
 * each scenario to prevent state leakage between scenarios.
 *
 * <p>Typical usage:
 * <pre>{@code
 *   // In a Given step:
 *   scenarioContext.set(ScenarioContext.CUSTOMER_ID, registrationResponse.jsonPath().getString("customerId"));
 *
 *   // In a subsequent When/Then step:
 *   String customerId = scenarioContext.get(ScenarioContext.CUSTOMER_ID);
 * }</pre>
 */
public class ScenarioContext {

    // ─── Well-known context keys ──────────────────────────────────────────────
    public static final String CUSTOMER_ID     = "CUSTOMER_ID";
    public static final String CUSTOMER_EMAIL  = "CUSTOMER_EMAIL";
    public static final String DOCUMENT_ID     = "DOCUMENT_ID";
    public static final String KYC_CASE_ID     = "KYC_CASE_ID";
    public static final String LAST_RESPONSE   = "LAST_RESPONSE";
    public static final String ACCOUNT_NUMBER  = "ACCOUNT_NUMBER";
    public static final String RISK_ASSESSMENT = "RISK_ASSESSMENT";

    private final Map<String, Object> store = new HashMap<>();

    /** Stores any typed value under the given key. */
    public void set(String key, Object value) {
        ValidationUtil.requireNonEmpty(key, "key", "ScenarioContext.set");
        store.put(key, value);
    }

    /**
     * Retrieves a value from the context, casting to the expected type.
     *
     * @param key  the context key
     * @param <T>  the expected return type
     * @return the stored value, or {@code null} if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        ValidationUtil.requireNonEmpty(key, "key", "ScenarioContext.get");
        return (T) store.get(key);
    }

    /**
     * Returns {@code true} if the given key is present in the context.
     */
    public boolean contains(String key) {
        ValidationUtil.requireNonEmpty(key, "key", "ScenarioContext.contains");
        return store.containsKey(key);
    }

    /**
     * Convenience method to store the most recent RestAssured response.
     * Used by {@link com.kyc.automation.hooks.ApiHooks} for on-failure logging.
     */
    public void setLastResponse(Response response) {
        ValidationUtil.requireNonNull(response, "response", "ScenarioContext.setLastResponse");
        store.put(LAST_RESPONSE, response);
    }

    /**
     * Returns the most recently stored RestAssured response, or {@code null} if none.
     */
    public Response getLastResponse() {
        return get(LAST_RESPONSE);
    }

    /** Clears all stored state. Called by {@link com.kyc.automation.hooks.ApiHooks} before each scenario. */
    public void reset() {
        store.clear();
    }
}
