package com.example.documentservice.customer.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class RestCustomerClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private RestCustomerClient restCustomerClient;

    @BeforeEach
    void setUp() {
        restCustomerClient = new RestCustomerClient(builder, "http://customer-service");
    }

    @Test
    void customerExists_returnsTrueWhenGetSucceeds() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", customerId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertThat(restCustomerClient.customerExists(customerId)).isTrue();
    }

    @Test
    void customerExists_returnsFalseWhenNotFound() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", customerId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientResponseException(
                "not found", 404, "Not Found", null, null, null));

        assertThat(restCustomerClient.customerExists(customerId)).isFalse();
    }

    @Test
    void customerExists_rethrowsNonNotFoundError() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", customerId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientResponseException(
                "server", 500, "Server Error", null, null, null));

        assertThatThrownBy(() -> restCustomerClient.customerExists(customerId))
                .isInstanceOf(RestClientResponseException.class)
                .extracting(ex -> ((RestClientResponseException) ex).getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(500));
    }
}
