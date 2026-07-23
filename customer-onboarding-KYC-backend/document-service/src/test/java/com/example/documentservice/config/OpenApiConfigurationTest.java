package com.example.documentservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenApiConfigurationTest {

    @Test
    void documentServiceOpenApi_containsExpectedMetadata() {
        OpenApiConfiguration configuration = new OpenApiConfiguration();

        var openApi = configuration.documentServiceOpenAPI();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("Document Service API");
        assertThat(openApi.getServers()).hasSize(1);
        assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("http://localhost:8082");
    }
}
