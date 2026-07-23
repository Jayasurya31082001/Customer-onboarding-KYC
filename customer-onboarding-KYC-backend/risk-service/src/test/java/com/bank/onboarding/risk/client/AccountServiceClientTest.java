package com.bank.onboarding.risk.client;

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

import com.bank.onboarding.risk.model.Disposition;

@ExtendWith(MockitoExtension.class)
class AccountServiceClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() {
        accountServiceClient = new AccountServiceClient(builder, "http://account-service");
    }

    @Test
    void publishRiskAssessed_postsIngressRequest() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://account-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        accountServiceClient.publishRiskAssessed("cust-1", "cust1@bank.test", Disposition.AUTO_APPROVE, 77);
    }

    @Test
    void publishRiskAssessed_rethrowsWhenDownstreamFails() {
        when(builder.baseUrl("http://account-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenThrow(new RuntimeException("downstream failed"));

        assertThatThrownBy(() -> accountServiceClient.publishRiskAssessed(
                "cust-1",
                "cust1@bank.test",
                Disposition.AUTO_APPROVE,
                77))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("downstream failed");
    }
}
