package com.example.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableJpaAuditing
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        return RestClient.builder().requestFactory(requestFactory);
    }
}
