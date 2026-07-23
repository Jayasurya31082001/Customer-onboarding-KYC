package com.example.notificationservice.provider.client;

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
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.example.notificationservice.exception.NotificationDeliveryException;

@ExtendWith(MockitoExtension.class)
class RestNotificationSenderClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private RestNotificationSenderClient senderClient;

    @BeforeEach
    void setUp() {
        senderClient = new RestNotificationSenderClient(builder, "http://notification-provider");
    }

    @Test
    void send_postsNotificationRequest() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://notification-provider")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        senderClient.send("cust1@bank.test", "Hello", null);
    }

    @Test
    void fallbackSend_throwsNotificationDeliveryException() {
        assertThatThrownBy(() -> senderClient.fallbackSend(
                "cust1@bank.test",
                "Hello",
                "corr-1",
                new RuntimeException("downstream error")))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("provider unavailable");
    }
}
