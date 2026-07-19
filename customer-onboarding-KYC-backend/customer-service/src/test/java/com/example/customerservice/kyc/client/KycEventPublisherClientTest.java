package com.example.customerservice.kyc.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class KycEventPublisherClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private KycEventPublisherClient publisherClient;

    @BeforeEach
    void setUp() {
        publisherClient = new KycEventPublisherClient(builder, "http://kyc-service");
    }

    @Test
    void publishCustomerRegistered_postsIngressRequest() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://kyc-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        publisherClient.publishCustomerRegistered(new CustomerRegisteredIngressRequest(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "corr-1"));
    }
}
