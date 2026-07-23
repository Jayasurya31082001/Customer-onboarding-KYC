package com.example.accountservice.customer.client;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.web.client.RestClientException;

import com.example.accountservice.exception.DownstreamIntegrationException;

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
    void getCustomerEmail_returnsEmailWhenResponseIsPresent() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}/email", "cust-1")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CustomerServiceClient.CustomerEmailResponse.class))
                .thenReturn(new CustomerServiceClient.CustomerEmailResponse("cust1@bank.test"));

        String email = customerServiceClient.getCustomerEmail("cust-1");

        assertThat(email).isEqualTo("cust1@bank.test");
    }

    @Test
    void getCustomerEmail_wrapsClientFailure() {
        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> customerServiceClient.getCustomerEmail("cust-1"))
                .isInstanceOf(DownstreamIntegrationException.class)
                .hasMessageContaining("retrieve customer email");
    }

    @Test
    void updateOnboardingStatusToApproved_postsRequest() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        customerServiceClient.updateOnboardingStatusToApproved("cust-1");
    }
}
