package com.example.kycservice.document.client;

import java.util.UUID;

public interface DocumentClient {

    void assertDocumentExists(UUID documentId);

    DocumentDetails getDocumentDetails(UUID documentId);
}
