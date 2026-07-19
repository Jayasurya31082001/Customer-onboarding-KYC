package com.example.documentservice.document.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.documentservice.common.exception.DocumentStorageException;
import com.example.documentservice.common.exception.InvalidDocumentException;
import com.example.documentservice.common.exception.ResourceNotFoundException;
import com.example.documentservice.document.dto.DocumentDownloadResponse;
import com.example.documentservice.document.dto.DocumentMetadataResponse;
import com.example.documentservice.document.dto.DocumentUploadResponse;
import com.example.documentservice.document.service.DocumentService;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    @DisplayName("POST /api/documents returns 201 with Location header on success")
    void uploadDocument_validRequest_returns201WithLocation() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(documentService.uploadDocument(any(), any()))
                .thenReturn(new DocumentUploadResponse(documentId, customerId, "passport.pdf",
                        MediaType.APPLICATION_PDF_VALUE, 100L, now));

        MockMultipartFile filePart = new MockMultipartFile(
                "file", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());

        MockMultipartHttpServletRequestBuilder request = multipart("/api/documents")
                .file(filePart);
        request.param("customerId", customerId.toString());

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(documentId.toString())))
                .andExpect(jsonPath("$.documentId").value(documentId.toString()))
                .andExpect(jsonPath("$.fileName").value("passport.pdf"));
    }

    @Test
    @DisplayName("POST /api/documents returns 400 when file is invalid")
    void uploadDocument_invalidDocument_returns400() throws Exception {
        when(documentService.uploadDocument(any(), any()))
                .thenThrow(new InvalidDocumentException("Only PDF, JPEG, and PNG files are supported"));

        UUID customerId = UUID.randomUUID();
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "text".getBytes());

        MockMultipartHttpServletRequestBuilder request = multipart("/api/documents")
                .file(filePart);
        request.param("customerId", customerId.toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid document"))
                .andExpect(jsonPath("$.detail").value("Only PDF, JPEG, and PNG files are supported"));
    }

    @Test
    @DisplayName("POST /api/documents returns 404 when customer does not exist")
    void uploadDocument_customerNotFound_returns404() throws Exception {
        when(documentService.uploadDocument(any(), any()))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        UUID customerId = UUID.randomUUID();
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());

        MockMultipartHttpServletRequestBuilder request = multipart("/api/documents")
                .file(filePart);
        request.param("customerId", customerId.toString());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    @DisplayName("POST /api/documents returns 500 on storage failure")
    void uploadDocument_storageFailure_returns500() throws Exception {
        when(documentService.uploadDocument(any(), any()))
                .thenThrow(new DocumentStorageException("Failed to read uploaded document", new RuntimeException()));

        UUID customerId = UUID.randomUUID();
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());

        MockMultipartHttpServletRequestBuilder request = multipart("/api/documents")
                .file(filePart);
        request.param("customerId", customerId.toString());

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Document processing failed"));
    }

    @Test
    @DisplayName("GET /api/documents/{id} returns 200 with document bytes and content-disposition")
    void getDocument_existingDocument_returns200WithBytes() throws Exception {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        byte[] bytes = "pdf-bytes".getBytes();

        when(documentService.getDocument(documentId))
                .thenReturn(new DocumentDownloadResponse(documentId, customerId, "passport.pdf",
                        MediaType.APPLICATION_PDF_VALUE, (long) bytes.length, bytes));

        mockMvc.perform(get("/api/documents/{id}", documentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("passport.pdf")))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("GET /api/documents/{id} returns 404 when document does not exist")
    void getDocument_unknownDocument_returns404() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(documentService.getDocument(documentId))
                .thenThrow(new ResourceNotFoundException("Document not found: " + documentId));

        mockMvc.perform(get("/api/documents/{id}", documentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    @DisplayName("GET /api/documents/{id} returns 400 when documentId is not a valid UUID")
    void getDocument_invalidUuidFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/documents/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameter"));
    }

    @Test
    @DisplayName("GET /api/documents/customer/{customerId}/latest/download returns latest document bytes")
    void downloadLatestByCustomer_returns200WithBytes() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        byte[] bytes = "latest-pdf-bytes".getBytes();

        when(documentService.getLatestDocumentByCustomerId(customerId))
                .thenReturn(new DocumentDownloadResponse(
                        documentId,
                        customerId,
                        "latest-passport.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        (long) bytes.length,
                        bytes));

        mockMvc.perform(get("/api/documents/customer/{customerId}/latest/download", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("latest-passport.pdf")))
                .andExpect(content().bytes(bytes));
    }

        @Test
        @DisplayName("GET /api/documents/customer/{customerId}/latest returns latest metadata")
        void getLatestDocumentMetadataByCustomer_returns200() throws Exception {
                UUID customerId = UUID.randomUUID();
                UUID documentId = UUID.randomUUID();

                when(documentService.getLatestDocumentMetadataByCustomerId(customerId))
                                .thenReturn(new DocumentMetadataResponse(
                                                documentId,
                                                customerId,
                                                "passport.pdf",
                                                MediaType.APPLICATION_PDF_VALUE,
                                                100L));

                mockMvc.perform(get("/api/documents/customer/{customerId}/latest", customerId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.documentId").value(documentId.toString()))
                                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                                .andExpect(jsonPath("$.fileName").value("passport.pdf"));
        }
}
