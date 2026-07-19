package com.example.customerservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenApiConfigurationTest {

    @Test
    void customerServiceOpenApi_containsExpectedMetadata() {
        OpenApiConfiguration configuration = new OpenApiConfiguration();

        var openApi = configuration.customerServiceOpenAPI();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("Customer Service API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openApi.getServers()).hasSize(1);
        assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("http://localhost:8081");
    }
}
