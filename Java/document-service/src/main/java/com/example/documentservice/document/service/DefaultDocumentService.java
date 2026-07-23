package com.example.documentservice.document.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentservice.common.exception.DocumentStorageException;
import com.example.documentservice.common.exception.InvalidDocumentException;
import com.example.documentservice.common.exception.ResourceNotFoundException;
import com.example.documentservice.config.CacheConfig;
import com.example.documentservice.customer.client.CustomerClient;
import com.example.documentservice.document.dto.DocumentDownloadResponse;
import com.example.documentservice.document.dto.DocumentMetadataResponse;
import com.example.documentservice.document.dto.DocumentUploadResponse;
import com.example.documentservice.document.event.DocumentUploadedEvent;
import com.example.documentservice.document.model.CustomerDocument;
import com.example.documentservice.document.repository.DocumentRepository;

@Service
public class DefaultDocumentService implements DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDocumentService.class);

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final DocumentRepository documentRepository;
    private final CustomerClient customerClient;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DefaultDocumentService(DocumentRepository documentRepository,
                                  CustomerClient customerClient,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.documentRepository = documentRepository;
        this.customerClient = customerClient;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(UUID customerId, MultipartFile file) {
        if (!customerClient.customerExists(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        validateMultipartFile(file);
        String originalFileName = requireOriginalFileName(file);

        LocalDateTime now = LocalDateTime.now();
        CustomerDocument document = new CustomerDocument();
        document.setCustomerId(customerId);
        document.setFileName(originalFileName);
        document.setContentType(file.getContentType());
        document.setSizeInBytes(file.getSize());
        document.setContent(readFileContent(file));
        document.setCreatedAt(now);

        CustomerDocument savedDocument = documentRepository.save(document);
        String correlationId = MDC.get("correlationId");
        applicationEventPublisher.publishEvent(new DocumentUploadedEvent(
                savedDocument.getDocumentId(),
                savedDocument.getCustomerId(),
                savedDocument.getFileName(),
                savedDocument.getContentType(),
                now,
                correlationId
        ));

        LOGGER.info("Document uploaded. customerId={}, documentId={}, correlationId={}",
                savedDocument.getCustomerId(), savedDocument.getDocumentId(), correlationId);

        return new DocumentUploadResponse(
                savedDocument.getDocumentId(),
                savedDocument.getCustomerId(),
                savedDocument.getFileName(),
                savedDocument.getContentType(),
                savedDocument.getSizeInBytes(),
                savedDocument.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownloadResponse getDocument(UUID documentId) {
        CustomerDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        return new DocumentDownloadResponse(
                document.getDocumentId(),
                document.getCustomerId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeInBytes(),
                document.getContent()
        );
    }

            @Override
            @Transactional(readOnly = true)
            public DocumentDownloadResponse getLatestDocumentByCustomerId(UUID customerId) {
                CustomerDocument document = documentRepository.findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found for customerId: " + customerId));

            return new DocumentDownloadResponse(
                document.getDocumentId(),
                document.getCustomerId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeInBytes(),
                document.getContent()
            );
            }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.DOCUMENTS_CACHE, key = "#documentId + ':metadata'")
    public DocumentMetadataResponse getDocumentMetadata(UUID documentId) {
        CustomerDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        return new DocumentMetadataResponse(
                document.getDocumentId(),
                document.getCustomerId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeInBytes()
        );
    }

            @Override
            @Transactional(readOnly = true)
            public DocumentMetadataResponse getLatestDocumentMetadataByCustomerId(UUID customerId) {
            CustomerDocument document = documentRepository.findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found for customerId: " + customerId));

            return new DocumentMetadataResponse(
                document.getDocumentId(),
                document.getCustomerId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeInBytes()
            );
            }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("Document file is required");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new InvalidDocumentException("Document file name is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidDocumentException("Document must not exceed 5 MB");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidDocumentException("Only PDF, JPEG, and PNG files are supported");
        }

        validateFileSignature(file, file.getContentType());
    }

    private String requireOriginalFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new InvalidDocumentException("Document file name is required");
        }
        return originalFileName.trim();
    }

    private byte[] readFileContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new DocumentStorageException("Failed to read uploaded document", exception);
        }
    }

    private void validateFileSignature(MultipartFile file, String contentType) {
        byte[] bytes = readFileContent(file);
        boolean valid;
        if (MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
            valid = hasPrefix(bytes, new byte[] {0x25, 0x50, 0x44, 0x46});
        } else if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            valid = hasPrefix(bytes, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        } else if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            valid = hasPrefix(bytes, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        } else {
            valid = false;
        }

        if (!valid) {
            throw new InvalidDocumentException("Document content does not match the declared content type");
        }
    }

    private boolean hasPrefix(byte[] bytes, byte[] expectedPrefix) {
        if (bytes.length < expectedPrefix.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOf(bytes, expectedPrefix.length), expectedPrefix);
    }
}
