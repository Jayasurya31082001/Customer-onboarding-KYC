package com.example.kycservice.document.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.kycservice.common.exception.ResourceNotFoundException;

@Component
public class RestDocumentClient implements DocumentClient {

    private final RestClient.Builder restClientBuilder;
    private final String documentServiceBaseUrl;

    public RestDocumentClient(RestClient.Builder restClientBuilder,
                              @Value("${document.service.base-url}") String documentServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.documentServiceBaseUrl = documentServiceBaseUrl;
    }

    @Override
    public void assertDocumentExists(UUID documentId) {
        RestClient client = restClientBuilder.baseUrl(documentServiceBaseUrl).build();
        try {
            client.get()
                    .uri("/api/documents/{documentId}", documentId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Document not found: " + documentId);
            }
            throw exception;
        }
    }

    @Override
    public DocumentDetails getDocumentDetails(UUID documentId) {
        RestClient client = restClientBuilder.baseUrl(documentServiceBaseUrl).build();
        try {
            DocumentDetailsResponse response = client.get()
                    .uri("/api/documents/metadata/{documentId}", documentId)
                    .retrieve()
                    .body(DocumentDetailsResponse.class);

            if (response == null) {
                throw new ResourceNotFoundException("Document metadata not found: " + documentId);
            }

            return new DocumentDetails(
                    response.documentId(),
                    response.customerId(),
                    response.fileName(),
                    response.contentType(),
                    response.sizeInBytes()
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Document not found: " + documentId);
            }
            throw exception;
        }
    }

    private record DocumentDetailsResponse(
            UUID documentId,
            UUID customerId,
            String fileName,
            String contentType,
            long sizeInBytes
    ) {
    }
}
