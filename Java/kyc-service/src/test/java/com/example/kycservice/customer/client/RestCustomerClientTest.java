package com.example.kycservice.customer.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.kycservice.common.exception.ResourceNotFoundException;

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
    void getCustomer_returnsMappedCustomerProfile() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();
        CustomerServiceResponseFixture response = new CustomerServiceResponseFixture(
                customerId,
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "US",
                "KYC_IN_PROGRESS",
                "jane@example.com");

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/v1/customers/{customerId}", customerId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response.toServiceResponse());

        CustomerProfile profile = restCustomerClient.getCustomer(customerId);

        assertThat(profile.customerId()).isEqualTo(customerId);
        assertThat(profile.firstName()).isEqualTo("Jane");
        assertThat(profile.email()).isEqualTo("jane@example.com");
    }

    @Test
    void getCustomer_throwsNotFoundWhenNullBody() {
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
        when(responseSpec.body(any(Class.class))).thenReturn(null);

        assertThatThrownBy(() -> restCustomerClient.getCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCustomer_throwsNotFoundOn404() {
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
        when(responseSpec.body(any(Class.class))).thenThrow(new RestClientResponseException(
                "not found", 404, "Not Found", null, null, null));

        assertThatThrownBy(() -> restCustomerClient.getCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOnboardingStatus_postsWithHeaderWhenCorrelationPresent() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri("/api/v1/customers/{customerId}/status", customerId)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.header("X-Correlation-ID", "corr-1")).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        restCustomerClient.updateOnboardingStatus(customerId, "KYC_PASS", "corr-1");
    }

    @Test
    void updateOnboardingStatus_postsWithoutHeaderWhenCorrelationBlank() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri("/api/v1/customers/{customerId}/status", customerId)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        restCustomerClient.updateOnboardingStatus(customerId, "KYC_PASS", " ");
    }

    @Test
    void updateOnboardingStatus_throwsNotFoundOn404() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri("/api/v1/customers/{customerId}/status", customerId)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientResponseException(
                "not found", 404, "Not Found", null, null, null));

        assertThatThrownBy(() -> restCustomerClient.updateOnboardingStatus(customerId, "KYC_PASS", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOnboardingStatus_rethrowsOnNon404() {
        RestClient.RequestBodyUriSpec patchSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://customer-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.patch()).thenReturn(patchSpec);
        when(patchSpec.uri("/api/v1/customers/{customerId}/status", customerId)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientResponseException(
                "error", 500, "Server Error", null, null, null));

        assertThatThrownBy(() -> restCustomerClient.updateOnboardingStatus(customerId, "KYC_PASS", null))
                .isInstanceOf(RestClientResponseException.class)
                .extracting(ex -> ((RestClientResponseException) ex).getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(500));
    }

    private record CustomerServiceResponseFixture(
            UUID customerId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String nationality,
            String status,
            String email
    ) {
        private Object toServiceResponse() {
            try {
                Class<?> clazz = Class.forName("com.example.kycservice.customer.client.RestCustomerClient$CustomerServiceResponse");
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object response = constructor.newInstance();

                var setCustomerId = clazz.getDeclaredMethod("setCustomerId", UUID.class);
                var setFirstName = clazz.getDeclaredMethod("setFirstName", String.class);
                var setLastName = clazz.getDeclaredMethod("setLastName", String.class);
                var setDateOfBirth = clazz.getDeclaredMethod("setDateOfBirth", LocalDate.class);
                var setNationality = clazz.getDeclaredMethod("setNationality", String.class);
                var setStatus = clazz.getDeclaredMethod("setStatus", String.class);
                var setEmail = clazz.getDeclaredMethod("setEmail", String.class);

                setCustomerId.setAccessible(true);
                setFirstName.setAccessible(true);
                setLastName.setAccessible(true);
                setDateOfBirth.setAccessible(true);
                setNationality.setAccessible(true);
                setStatus.setAccessible(true);
                setEmail.setAccessible(true);

                setCustomerId.invoke(response, customerId);
                setFirstName.invoke(response, firstName);
                setLastName.invoke(response, lastName);
                setDateOfBirth.invoke(response, dateOfBirth);
                setNationality.invoke(response, nationality);
                setStatus.invoke(response, status);
                setEmail.invoke(response, email);
                return response;
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
