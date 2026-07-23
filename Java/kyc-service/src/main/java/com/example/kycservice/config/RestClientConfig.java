package com.example.kycservice.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableJpaAuditing
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));

        return RestClient.builder().requestFactory(requestFactory);
    }
}
