package com.kyc.automation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseApiConfig centralises RestAssured configuration for the Customer-Onboarding KYC test suite.
 *
 * <p>Each microservice in this project runs on a dedicated port (H2 in-memory DB, no external
 * dependencies). Service base-URI constants reflect {@code application.properties} defaults.
 * Override via environment variables for CI or non-default local setups.
 *
 * <p>There is NO WebDriver, browser, or UI-related code in this class or anywhere in this suite.
 */
public final class BaseApiConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BaseApiConfig.class);

    // ─── Service port constants (match application.properties defaults) ───────
    public static final int CUSTOMER_SERVICE_PORT  = intEnv("CUSTOMER_SERVICE_PORT",  8081);
    public static final int DOCUMENT_SERVICE_PORT  = intEnv("DOCUMENT_SERVICE_PORT",  8082);
    public static final int KYC_SERVICE_PORT       = intEnv("KYC_SERVICE_PORT",       8083);
    public static final int RISK_SERVICE_PORT      = intEnv("RISK_SERVICE_PORT",      8084);
    public static final int ACCOUNT_SERVICE_PORT   = intEnv("ACCOUNT_SERVICE_PORT",   8085);
    public static final int NOTIFICATION_SERVICE_PORT = intEnv("NOTIFICATION_SERVICE_PORT", 8086);

    // ─── Host defaults ────────────────────────────────────────────────────────
    public static final String BASE_HOST = System.getenv().getOrDefault("API_HOST", "http://localhost");

    // ─── Shared ObjectMapper with Java-time support ───────────────────────────
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private BaseApiConfig() { /* utility class – not instantiatable */ }

    // ── RequestSpecification builders ─────────────────────────────────────────

    /** JSON-default spec for the Customer Service (port 8081). */
    public static RequestSpecification customerServiceSpec() {
        return jsonSpec(CUSTOMER_SERVICE_PORT);
    }

    /** Multipart/form-data capable spec for the Document Service (port 8082). */
    public static RequestSpecification documentServiceSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_HOST)
                .setPort(DOCUMENT_SERVICE_PORT)
                .setRelaxedHTTPSValidation()
                .log(LogDetail.ALL)
                .build();
    }

    /** JSON-default spec for the KYC Service (port 8083). */
    public static RequestSpecification kycServiceSpec() {
        return jsonSpec(KYC_SERVICE_PORT);
    }

    /** JSON-default spec for the Risk Service (port 8084). */
    public static RequestSpecification riskServiceSpec() {
        return jsonSpec(RISK_SERVICE_PORT);
    }

    /** JSON-default spec for the Account Service (port 8085). */
    public static RequestSpecification accountServiceSpec() {
        return jsonSpec(ACCOUNT_SERVICE_PORT);
    }

    /** JSON-default spec for the Notification Service (port 8086). */
    public static RequestSpecification notificationServiceSpec() {
        return jsonSpec(NOTIFICATION_SERVICE_PORT);
    }

    // ── Static initialiser – global RestAssured defaults ─────────────────────
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        LOG.info("RestAssured global defaults initialised. BASE_HOST={}", BASE_HOST);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static RequestSpecification jsonSpec(int port) {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_HOST)
                .setPort(port)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .log(LogDetail.ALL)
                .build();
    }

    private static int intEnv(String name, int defaultValue) {
        String val = System.getenv(name);
        return (val != null && !val.isBlank()) ? Integer.parseInt(val.trim()) : defaultValue;
    }
}
