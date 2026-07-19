package com.example.documentservice.document.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.example.documentservice.document.dto.DocumentDownloadResponse;
import com.example.documentservice.document.dto.DocumentMetadataResponse;
import com.example.documentservice.document.dto.DocumentUploadResponse;

public interface DocumentService {

    DocumentUploadResponse uploadDocument(UUID customerId, MultipartFile file);

    DocumentDownloadResponse getDocument(UUID documentId);

    DocumentDownloadResponse getLatestDocumentByCustomerId(UUID customerId);

    DocumentMetadataResponse getDocumentMetadata(UUID documentId);

    DocumentMetadataResponse getLatestDocumentMetadataByCustomerId(UUID customerId);
}
