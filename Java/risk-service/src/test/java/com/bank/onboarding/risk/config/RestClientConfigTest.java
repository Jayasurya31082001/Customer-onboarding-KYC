package com.bank.onboarding.risk.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RestClientConfigTest {

    @Test
    void restClientBuilder_isCreated() {
        RestClientConfig config = new RestClientConfig();

        RestClient.Builder builder = config.restClientBuilder();

        assertThat(builder).isNotNull();
    }
}
