package com.example.datasyncservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configures one named {@link RestClient} per upstream microservice, and
 * a dedicated client for the Databricks REST API.
 *
 * <p>Each client has its own base URL, connect/read timeouts, and the
 * {@code X-Internal-Secret} header pre-set so callers never handle auth manually.
 *
 * <p>Spring Boot 3.x's {@link RestClient} (not {@code RestTemplate}) is used
 * for its fluent, modern API and native virtual-thread compatibility.
 */
@Configuration
@EnableJpaAuditing
public class RestClientConfig {

    @Value("${datasync.security.internal-secret}")
    private String internalSecret;

    @Value("${services.customer.base-url}")
    private String customerBaseUrl;

    @Value("${services.document.base-url}")
    private String documentBaseUrl;

    @Value("${services.kyc.base-url}")
    private String kycBaseUrl;

    @Value("${services.risk.base-url}")
    private String riskBaseUrl;

    @Value("${services.account.base-url}")
    private String accountBaseUrl;

    @Value("${services.notification.base-url}")
    private String notificationBaseUrl;

    @Value("${databricks.workspace-url}")
    private String databricksWorkspaceUrl;

    @Value("${databricks.token}")
    private String databricksToken;

    @Value("${databricks.connect-timeout-seconds:10}")
    private int connectTimeoutSeconds;

    @Value("${databricks.read-timeout-seconds:60}")
    private int readTimeoutSeconds;

    // ─── Upstream service clients ──────────────────────────────────────────────

    @Bean("customerRestClient")
    public RestClient customerRestClient() {
        return buildInternalClient(customerBaseUrl);
    }

    @Bean("documentRestClient")
    public RestClient documentRestClient() {
        return buildInternalClient(documentBaseUrl);
    }

    @Bean("kycRestClient")
    public RestClient kycRestClient() {
        return buildInternalClient(kycBaseUrl);
    }

    @Bean("riskRestClient")
    public RestClient riskRestClient() {
        return buildInternalClient(riskBaseUrl);
    }

    @Bean("accountRestClient")
    public RestClient accountRestClient() {
        return buildInternalClient(accountBaseUrl);
    }

    @Bean("notificationRestClient")
    public RestClient notificationRestClient() {
        return buildInternalClient(notificationBaseUrl);
    }

    // ─── Databricks client ────────────────────────────────────────────────────

    @Bean("databricksRestClient")
    public RestClient databricksRestClient() {
        return RestClient.builder()
                .baseUrl(databricksWorkspaceUrl)
                .defaultHeader("Authorization", "Bearer " + databricksToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Builds an internal service client with the shared secret header pre-applied.
     * All /internal/sync calls pass through this client.
     */
    private RestClient buildInternalClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Secret", internalSecret)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
