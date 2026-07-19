package com.example.documentservice.document.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentservice.common.exception.DocumentStorageException;
import com.example.documentservice.common.exception.InvalidDocumentException;
import com.example.documentservice.common.exception.ResourceNotFoundException;
import com.example.documentservice.customer.client.CustomerClient;
import com.example.documentservice.document.dto.DocumentDownloadResponse;
import com.example.documentservice.document.dto.DocumentMetadataResponse;
import com.example.documentservice.document.dto.DocumentUploadResponse;
import com.example.documentservice.document.event.DocumentUploadedEvent;
import com.example.documentservice.document.model.CustomerDocument;
import com.example.documentservice.document.repository.DocumentRepository;

@ExtendWith(MockitoExtension.class)
class DefaultDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DefaultDocumentService documentService;

    @Captor
    private ArgumentCaptor<CustomerDocument> documentCaptor;

    @Captor
    private ArgumentCaptor<DocumentUploadedEvent> eventCaptor;

    @Test
    @DisplayName("uploadDocument valid file persists document and publishes event")
    void uploadDocument_validFile_persistsDocumentAndPublishesEvent() throws Exception {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.7\nmock".getBytes()
        );

        when(customerClient.customerExists(customerId)).thenReturn(true);
        when(documentRepository.save(any(CustomerDocument.class))).thenAnswer(invocation -> {
            CustomerDocument document = invocation.getArgument(0);
            if (document.getDocumentId() == null) {
                document.setDocumentId(UUID.randomUUID());
            }
            return document;
        });

        DocumentUploadResponse response = documentService.uploadDocument(customerId, file);

        verify(documentRepository).save(documentCaptor.capture());
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        CustomerDocument savedDocument = documentCaptor.getValue();
        DocumentUploadedEvent event = eventCaptor.getValue();

        assertThat(savedDocument.getDocumentId()).isNotNull();
        assertThat(savedDocument.getCustomerId()).isEqualTo(customerId);
        assertThat(savedDocument.getFileName()).isEqualTo("passport.pdf");
        assertThat(savedDocument.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(savedDocument.getSizeInBytes()).isEqualTo(file.getSize());
        assertThat(savedDocument.getContent()).isEqualTo(file.getBytes());
        assertThat(savedDocument.getCreatedAt()).isNotNull();

        assertThat(response.documentId()).isEqualTo(savedDocument.getDocumentId());
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.fileName()).isEqualTo("passport.pdf");
        assertThat(response.contentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(response.sizeInBytes()).isEqualTo(file.getSize());

        assertThat(event.documentId()).isEqualTo(savedDocument.getDocumentId());
        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.fileName()).isEqualTo("passport.pdf");
        assertThat(event.contentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("uploadDocument missing customer throws not found")
    void uploadDocument_missingCustomer_throwsNotFound() {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.7\nmock".getBytes()
        );

        when(customerClient.customerExists(customerId)).thenReturn(false);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(documentRepository, never()).save(any(CustomerDocument.class));
    }

    @Test
    @DisplayName("uploadDocument empty file throws invalid document")
    void uploadDocument_emptyFile_throwsInvalidDocument() {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
        );

        when(customerClient.customerExists(customerId)).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Document file is required");
    }

    @Test
    @DisplayName("uploadDocument blank filename throws invalid document")
    void uploadDocument_blankFileName_throwsInvalidDocument() {
        UUID customerId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file",
                "",
                MediaType.APPLICATION_PDF_VALUE,
                "sample-pdf".getBytes()
        );

        when(customerClient.customerExists(customerId)).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Document file name is required");
    }

    @Test
    @DisplayName("uploadDocument oversized file throws invalid document")
    void uploadDocument_oversizedFile_throwsInvalidDocument() {
        UUID customerId = UUID.randomUUID();
        byte[] oversizedContent = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                oversizedContent
        );

        when(customerClient.customerExists(customerId)).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Document must not exceed 5 MB");
    }

    @Test
    @DisplayName("uploadDocument unsupported type throws invalid document")
    void uploadDocument_unsupportedType_throwsInvalidDocument() {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "plain-text".getBytes()
        );

        when(customerClient.customerExists(customerId)).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Only PDF, JPEG, and PNG files are supported");
    }

    @Test
    @DisplayName("uploadDocument io failure throws storage exception")
    void uploadDocument_ioFailure_throwsStorageException() {
        UUID customerId = UUID.randomUUID();
        MultipartFile file = new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return "passport.pdf";
            }

            @Override
            public String getContentType() {
                return MediaType.APPLICATION_PDF_VALUE;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 10;
            }

            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("boom");
            }

            @Override
            public InputStream getInputStream() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void transferTo(java.io.File dest) {
                throw new UnsupportedOperationException();
            }
        };

        when(customerClient.customerExists(customerId)).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(customerId, file))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to read uploaded document");
    }

    @Test
    @DisplayName("getDocument existing id returns content")
    void getDocument_existingId_returnsContent() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CustomerDocument document = new CustomerDocument();
        document.setDocumentId(documentId);
        document.setCustomerId(customerId);
        document.setFileName("passport.png");
        document.setContentType(MediaType.IMAGE_PNG_VALUE);
        document.setSizeInBytes(12);
        document.setContent("hello-world!".getBytes());

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        DocumentDownloadResponse response = documentService.getDocument(documentId);

        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.fileName()).isEqualTo("passport.png");
        assertThat(response.contentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
        assertThat(response.sizeInBytes()).isEqualTo(12);
        assertThat(response.content()).isEqualTo("hello-world!".getBytes());
    }

    @Test
    @DisplayName("getDocument missing id throws not found")
    void getDocument_missingId_throwsNotFound() {
        UUID documentId = UUID.randomUUID();
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(documentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    @DisplayName("getLatestDocumentMetadataByCustomerId returns newest document metadata")
    void getLatestDocumentMetadataByCustomerId_returnsMetadata() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        CustomerDocument latest = new CustomerDocument();
        latest.setDocumentId(documentId);
        latest.setCustomerId(customerId);
        latest.setFileName("id-proof.pdf");
        latest.setContentType(MediaType.APPLICATION_PDF_VALUE);
        latest.setSizeInBytes(500);

        when(documentRepository.findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(customerId))
                .thenReturn(Optional.of(latest));

        DocumentMetadataResponse response = documentService.getLatestDocumentMetadataByCustomerId(customerId);

        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.fileName()).isEqualTo("id-proof.pdf");
        assertThat(response.contentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(response.sizeInBytes()).isEqualTo(500);
    }

    @Test
    @DisplayName("getLatestDocumentByCustomerId returns newest document content")
    void getLatestDocumentByCustomerId_returnsContent() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        byte[] content = "latest-document-content".getBytes();

        CustomerDocument latest = new CustomerDocument();
        latest.setDocumentId(documentId);
        latest.setCustomerId(customerId);
        latest.setFileName("latest-passport.pdf");
        latest.setContentType(MediaType.APPLICATION_PDF_VALUE);
        latest.setSizeInBytes(content.length);
        latest.setContent(content);

        when(documentRepository.findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(customerId))
                .thenReturn(Optional.of(latest));

        DocumentDownloadResponse response = documentService.getLatestDocumentByCustomerId(customerId);

        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.fileName()).isEqualTo("latest-passport.pdf");
        assertThat(response.contentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(response.sizeInBytes()).isEqualTo(content.length);
        assertThat(response.content()).isEqualTo(content);
    }
}
