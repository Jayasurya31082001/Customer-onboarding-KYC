package com.kyc.automation.util;

import io.restassured.http.Headers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiClientUtilTest {

    @Test
    @DisplayName("buildDefaultHeaders includes authorization header when auth token is provided")
    void testBuildDefaultHeadersWithToken() {
        Headers headers = ApiClientUtil.buildDefaultHeaders("token123");
        assertThat(headers.getValue("Authorization")).isEqualTo("Bearer token123");
        assertThat(headers.getValue("Content-Type")).contains("application/json");
    }

    @Test
    @DisplayName("buildDefaultHeaders omits authorization header when token is null or blank")
    void testBuildDefaultHeadersWithoutToken() {
        Headers headers = ApiClientUtil.buildDefaultHeaders(null);
        assertThat(headers.hasHeaderWithName("Authorization")).isFalse();
        assertThat(headers.getValue("Content-Type")).contains("application/json");
    }

    @Test
    @DisplayName("resolveCorrelationId returns existing correlation ID when non-blank")
    void testResolveCorrelationIdWithProvidedId() {
        String id = ApiClientUtil.resolveCorrelationId("custom-corr-id");
        assertThat(id).isEqualTo("custom-corr-id");
    }

    @Test
    @DisplayName("resolveCorrelationId generates new UUID string when correlation ID is null or blank")
    void testResolveCorrelationIdWithNullOrBlank() {
        String id1 = ApiClientUtil.resolveCorrelationId(null);
        assertThat(id1).isNotBlank();

        String id2 = ApiClientUtil.resolveCorrelationId("   ");
        assertThat(id2).isNotBlank();
        assertThat(id1).isNotEqualTo(id2);
    }
}
