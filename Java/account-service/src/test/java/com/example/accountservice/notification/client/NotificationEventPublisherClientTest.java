package com.example.accountservice.notification.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private NotificationEventPublisherClient publisherClient;

    @BeforeEach
    void setUp() {
        publisherClient = new NotificationEventPublisherClient(builder, "http://notification-service");
    }

    @Test
    void publishAccountCreated_postsIngressRequest() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://notification-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        publisherClient.publishAccountCreated(new AccountCreatedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                "12345678",
                "110011"));
    }

    @Test
    void publishAccountCreated_propagatesDownstreamFailure() {
        when(builder.baseUrl("http://notification-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenThrow(new RuntimeException("downstream failed"));

        assertThatThrownBy(() -> publisherClient.publishAccountCreated(new AccountCreatedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                "12345678",
                "110011")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("downstream failed");
    }
}
