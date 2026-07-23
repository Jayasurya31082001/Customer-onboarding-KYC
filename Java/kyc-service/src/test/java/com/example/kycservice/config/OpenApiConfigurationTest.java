package com.example.kycservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenApiConfigurationTest {

    @Test
    void kycServiceOpenApi_containsExpectedMetadata() {
        OpenApiConfiguration configuration = new OpenApiConfiguration();

        var openApi = configuration.kycServiceOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("KYC Service API");
        assertThat(openApi.getServers()).hasSize(1);
        assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("http://localhost:8083");
    }
}
