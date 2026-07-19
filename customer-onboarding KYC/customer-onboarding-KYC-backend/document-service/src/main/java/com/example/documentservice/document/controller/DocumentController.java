package com.example.documentservice.document.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.documentservice.document.dto.DocumentDownloadResponse;
import com.example.documentservice.document.dto.DocumentMetadataResponse;
import com.example.documentservice.document.dto.DocumentUploadRequest;
import com.example.documentservice.document.dto.DocumentUploadResponse;
import com.example.documentservice.document.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document upload and retrieval endpoints for KYC evidence")
public class DocumentController {

    private final DocumentService documentService;

        public DocumentController(DocumentService documentService) {
                this.documentService = documentService;
        }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a customer document",
            description = "Uploads a PDF, JPEG, or PNG document for an existing customer. Maximum file size is 5 MB."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Multipart document upload payload",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = DocumentUploadRequest.class))
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Document uploaded successfully",
                    content = @Content(schema = @Schema(implementation = DocumentUploadResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid multipart request or unsupported file"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal server error")
    })
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @Parameter(hidden = true)
            @RequestParam("customerId") UUID customerId,
            @Parameter(hidden = true)
            @RequestPart("file") MultipartFile file) {
        DocumentUploadResponse response = documentService.uploadDocument(customerId, file);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{documentId}")
                .buildAndExpand(response.documentId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{documentId}")
    @Operation(
            summary = "Download a customer document",
            description = "Retrieves the raw document bytes and responds with the stored content type and filename."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document returned successfully",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                                    schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                                    schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                                    schema = @Schema(type = "string", format = "binary"))
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal server error")
    })
    public ResponseEntity<byte[]> getDocument(
            @Parameter(description = "Document UUID to retrieve",
                    example = "c50e0f65-fdc5-4b0c-bf6a-60287f6430c7",
                    required = true)
            @PathVariable UUID documentId) {
        DocumentDownloadResponse document = documentService.getDocument(documentId);
        MediaType mediaType = MediaType.parseMediaType(document.contentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(document.sizeInBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(document.fileName())
                        .build()
                        .toString())
                .body(document.content());
    }

            @GetMapping("/customer/{customerId}/latest/download")
            @Operation(
                    summary = "Download latest customer document",
                    description = "Retrieves raw bytes for the latest uploaded document belonging to the customer."
            )
            @ApiResponses({
                    @ApiResponse(
                            responseCode = "200",
                            description = "Latest customer document returned successfully",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                                            schema = @Schema(type = "string", format = "binary")),
                                    @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                                            schema = @Schema(type = "string", format = "binary")),
                                    @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                                            schema = @Schema(type = "string", format = "binary"))
                            }
                    ),
                    @ApiResponse(responseCode = "404", description = "No document found for the customer"),
                    @ApiResponse(responseCode = "500", description = "Unexpected internal server error")
            })
            public ResponseEntity<byte[]> downloadLatestByCustomer(
                    @Parameter(description = "Customer UUID to retrieve latest uploaded document for",
                            example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
                            required = true)
                    @PathVariable UUID customerId) {
                DocumentDownloadResponse document = documentService.getLatestDocumentByCustomerId(customerId);
                MediaType mediaType = MediaType.parseMediaType(document.contentType());

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .contentLength(document.sizeInBytes())
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                                .filename(document.fileName())
                                .build()
                                .toString())
                        .body(document.content());
            }

    @GetMapping("/metadata/{documentId}")
    @Operation(
            summary = "Get document metadata",
            description = "Retrieves metadata (file name, content type, size) for a document without downloading the binary content."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document metadata returned successfully",
                    content = @Content(schema = @Schema(implementation = DocumentMetadataResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal server error")
    })
    public ResponseEntity<DocumentMetadataResponse> getDocumentMetadata(
            @Parameter(description = "Document UUID to retrieve metadata for",
                    example = "c50e0f65-fdc5-4b0c-bf6a-60287f6430c7",
                    required = true)
            @PathVariable UUID documentId) {
        DocumentMetadataResponse metadata = documentService.getDocumentMetadata(documentId);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/customer/{customerId}/latest")
    @Operation(
            summary = "Get latest document metadata by customer",
            description = "Retrieves metadata for the latest uploaded document belonging to the customer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Latest document metadata returned successfully",
                    content = @Content(schema = @Schema(implementation = DocumentMetadataResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "No document found for the customer"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal server error")
    })
    public ResponseEntity<DocumentMetadataResponse> getLatestDocumentMetadataByCustomer(
            @Parameter(description = "Customer UUID to retrieve latest document metadata for",
                    example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
                    required = true)
            @PathVariable UUID customerId) {
        DocumentMetadataResponse metadata = documentService.getLatestDocumentMetadataByCustomerId(customerId);
        return ResponseEntity.ok(metadata);
    }
}
