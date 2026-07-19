package com.example.kycservice.risk.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.example.kycservice.kyc.model.KycStatus;

@ExtendWith(MockitoExtension.class)
class RestRiskClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private RestRiskClient riskClient;

    @BeforeEach
    void setUp() {
        riskClient = new RestRiskClient(builder, "http://risk-service");
    }

    @Test
    void publishKycCompleted_postsWithCorrelationHeaderWhenPresent() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://risk-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri("/api/internal/events/kyc-completed")).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.header("X-Correlation-ID", "corr-1")).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        riskClient.publishKycCompleted(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                KycStatus.PASS,
                LocalDateTime.now(),
                "corr-1",
                "US",
                LocalDate.of(1990, 1, 1),
                false,
                false,
                "jane@example.com");
    }

    @Test
    void publishKycCompleted_postsWithoutCorrelationHeaderWhenBlank() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://risk-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri("/api/internal/events/kyc-completed")).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        riskClient.publishKycCompleted(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                KycStatus.FAIL,
                LocalDateTime.now(),
                " ",
                "US",
                LocalDate.of(1990, 1, 1),
                true,
                true,
                "jane@example.com");
    }
}
