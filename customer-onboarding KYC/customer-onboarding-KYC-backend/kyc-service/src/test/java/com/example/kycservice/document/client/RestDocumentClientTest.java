package com.example.kycservice.document.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.kycservice.common.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class RestDocumentClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private RestDocumentClient restDocumentClient;

    @BeforeEach
    void setUp() {
        restDocumentClient = new RestDocumentClient(builder, "http://document-service");
    }

    @Test
    void assertDocumentExists_succeedsForExistingDocument() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID documentId = UUID.randomUUID();

        when(builder.baseUrl("http://document-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/documents/{documentId}", documentId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        restDocumentClient.assertDocumentExists(documentId);
    }

    @Test
    void assertDocumentExists_throwsNotFoundOn404() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID documentId = UUID.randomUUID();

        when(builder.baseUrl("http://document-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/documents/{documentId}", documentId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientResponseException(
                "not found", 404, "Not Found", null, null, null));

        assertThatThrownBy(() -> restDocumentClient.assertDocumentExists(documentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDocumentDetails_returnsMappedRecord() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(builder.baseUrl("http://document-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/documents/metadata/{documentId}", documentId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(new DocumentDetailsResponseFixture(
                documentId,
                customerId,
                "passport.pdf",
                "application/pdf",
                123L
        ).toResponse());

        DocumentDetails details = restDocumentClient.getDocumentDetails(documentId);

        assertThat(details.documentId()).isEqualTo(documentId);
        assertThat(details.customerId()).isEqualTo(customerId);
        assertThat(details.fileName()).isEqualTo("passport.pdf");
    }

    @Test
    void getDocumentDetails_throwsNotFoundOnNullBody() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID documentId = UUID.randomUUID();

        when(builder.baseUrl("http://document-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/documents/metadata/{documentId}", documentId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(null);

        assertThatThrownBy(() -> restDocumentClient.getDocumentDetails(documentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDocumentDetails_throwsNotFoundOn404() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        UUID documentId = UUID.randomUUID();

        when(builder.baseUrl("http://document-service")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.get()).thenReturn(getSpec);
        when(getSpec.uri("/api/documents/metadata/{documentId}", documentId)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenThrow(new RestClientResponseException(
                "not found", 404, "Not Found", null, null, null));

        assertThatThrownBy(() -> restDocumentClient.getDocumentDetails(documentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private record DocumentDetailsResponseFixture(
            UUID documentId,
            UUID customerId,
            String fileName,
            String contentType,
            long sizeInBytes
    ) {
        private Object toResponse() {
            try {
                Class<?> clazz = Class.forName("com.example.kycservice.document.client.RestDocumentClient$DocumentDetailsResponse");
                var constructor = clazz.getDeclaredConstructor(UUID.class, UUID.class, String.class, String.class, long.class);
                constructor.setAccessible(true);
                return constructor.newInstance(documentId, customerId, fileName, contentType, sizeInBytes);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
