package com.bank.onboarding.risk.client;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.bank.onboarding.risk.exception.DownstreamIntegrationException;

@ExtendWith(MockitoExtension.class)
class CustomerServiceClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private CustomerServiceClient customerServiceClient;

    @BeforeEach
    void setUp() {
        customerServiceClient = new CustomerServiceClient(builder, "http://customer-service");
    }

    @Test
    void updateOnboardingStatusToManualReview_patchesCustomerStatus() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri(eq("/api/v1/customers/{customerId}/status"), any(UUID.class))).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        customerServiceClient.updateOnboardingStatusToManualReview("550e8400-e29b-41d4-a716-446655440001");
    }

    @Test
    void updateOnboardingStatusToRejected_wrapsRestClientException() {
        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenThrow(new RestClientException("downstream failed"));

        assertThatThrownBy(() -> customerServiceClient.updateOnboardingStatusToRejected("550e8400-e29b-41d4-a716-446655440000"))
                .isInstanceOf(DownstreamIntegrationException.class)
                .hasMessageContaining("REJECTED");
    }

    @Test
    void updateOnboardingStatusToRejected_patchesCustomerStatus() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri(eq("/api/v1/customers/{customerId}/status"), any(UUID.class))).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        customerServiceClient.updateOnboardingStatusToRejected("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void getCustomerProfile_returnsResponseWhenPresent() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        CustomerServiceClient.CustomerProfile expected = new CustomerServiceClient.CustomerProfile(
                "cust-1",
                LocalDate.of(1990, 2, 3),
                "IN",
                "cust1@bank.test");

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", "cust-1")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CustomerServiceClient.CustomerProfile.class)).thenReturn(expected);

        CustomerServiceClient.CustomerProfile actual = customerServiceClient.getCustomerProfile("cust-1");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getCustomerProfile_throwsWhenResponseIsNull() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", "cust-1")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CustomerServiceClient.CustomerProfile.class)).thenReturn(null);

        assertThatThrownBy(() -> customerServiceClient.getCustomerProfile("cust-1"))
                .isInstanceOf(DownstreamIntegrationException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void getCustomerProfile_wrapsRestClientException() {
        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenThrow(new RestClientException("service unavailable"));

        assertThatThrownBy(() -> customerServiceClient.getCustomerProfile("cust-1"))
                .isInstanceOf(DownstreamIntegrationException.class)
                .hasMessageContaining("fetch customer profile");
    }
}
