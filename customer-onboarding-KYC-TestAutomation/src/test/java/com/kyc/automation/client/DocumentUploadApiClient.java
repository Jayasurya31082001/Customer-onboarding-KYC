package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.util.ApiClientUtil;
import com.kyc.automation.util.ValidationUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * API client for the Document Service (port 8082).
 *
 * <p>
 * Covers:
 * <ul>
 * <li>POST /api/documents – multipart file upload</li>
 * <li>GET /api/documents/{documentId} – download raw document bytes</li>
 * <li>GET /api/documents/metadata/{documentId} – document metadata</li>
 * <li>GET /api/documents/customer/{id}/latest – latest document metadata by
 * customer</li>
 * </ul>
 *
 * <p>
 * File upload is performed via multipart/form-data (no browser required).
 */
public class DocumentUploadApiClient {

    private final RequestSpecification spec;

    public DocumentUploadApiClient() {
        this.spec = BaseApiConfig.documentServiceSpec();
    }

    // ─── Upload ───────────────────────────────────────────────────────────────

    /**
     * Uploads a real {@link File} on behalf of the given customer.
     *
     * @param customerId UUID string of the customer who owns the document
     * @param file       the file to upload
     * @param mimeType   MIME type to send as the part's content type (e.g.
     *                   "application/pdf")
     */
    public Response uploadDocument(String customerId, File file, String mimeType) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "DocumentUploadApiClient.uploadDocument");
        ValidationUtil.requireNonNull(file, "file", "DocumentUploadApiClient.uploadDocument");
        ValidationUtil.requireNonEmpty(mimeType, "mimeType", "DocumentUploadApiClient.uploadDocument");

        return ApiClientUtil.executeMultipartPost(spec, "/api/documents", "customerId", customerId, "file", file,
                mimeType);
    }

    /**
     * Convenience overload: creates an in-memory temp file from raw bytes and
     * uploads it.
     *
     * @param customerId UUID string
     * @param content    raw bytes to write into the temp file
     * @param fileName   name to give the temp file (extension determines MIME hint)
     * @param mimeType   MIME type header for the multipart part
     */
    public Response uploadDocumentBytes(String customerId,
            byte[] content,
            String fileName,
            String mimeType) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "DocumentUploadApiClient.uploadDocumentBytes");
        ValidationUtil.requireNonEmpty(content, "content", "DocumentUploadApiClient.uploadDocumentBytes");
        ValidationUtil.requireNonEmpty(fileName, "fileName", "DocumentUploadApiClient.uploadDocumentBytes");
        ValidationUtil.requireNonEmpty(mimeType, "mimeType", "DocumentUploadApiClient.uploadDocumentBytes");

        File tmp = createTempFile(content, fileName);
        try {
            return uploadDocument(customerId, tmp, mimeType);
        } finally {
            tmp.delete();
        }
    }

    // ─── Retrieval ────────────────────────────────────────────────────────────

    /**
     * Downloads the raw bytes of a document.
     */
    public Response downloadDocument(String documentId) {
        ValidationUtil.requireNonEmpty(documentId, "documentId", "DocumentUploadApiClient.downloadDocument");

        return ApiClientUtil.executeGet(spec, "/api/documents/{documentId}", documentId);
    }

    /**
     * Fetches document metadata (filename, contentType, size) without downloading
     * content.
     */
    public Response getDocumentMetadata(String documentId) {
        ValidationUtil.requireNonEmpty(documentId, "documentId", "DocumentUploadApiClient.getDocumentMetadata");

        return ApiClientUtil.executeGetWithAccept(spec, "application/json", "/api/documents/metadata/{documentId}",
                documentId);
    }

    /**
     * Returns metadata for the latest document uploaded by a customer.
     */
    public Response getLatestDocumentMetadataByCustomer(String customerId) {
        ValidationUtil.requireNonEmpty(customerId, "customerId",
                "DocumentUploadApiClient.getLatestDocumentMetadataByCustomer");

        return ApiClientUtil.executeGetWithAccept(spec, "application/json",
                "/api/documents/customer/{customerId}/latest", customerId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates a minimal valid PDF byte array (just enough for upload acceptance).
     */
    public static byte[] minimalPdfContent() {
        return "%PDF-1.4 minimal test document".getBytes(StandardCharsets.UTF_8);
    }

    /** Creates a minimal JPEG byte array (JPEG magic bytes). */
    public static byte[] minimalJpegContent() {
        // JPEG magic bytes: FF D8 FF E0
        return new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01 };
    }

    /** Creates a minimal PNG byte array (PNG magic bytes). */
    public static byte[] minimalPngContent() {
        // PNG magic bytes: 8 bytes header
        return new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00 };
    }

    /** Generates a byte array larger than 5 MB to trigger the file size limit. */
    public static byte[] oversizedContent() {
        // 5 MB + 1 byte
        return new byte[5 * 1024 * 1024 + 1];
    }

    private static File createTempFile(byte[] content, String fileName) {
        try {
            String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".tmp";
            File tmp = File.createTempFile("kyc-test-", suffix);
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(content);
            }
            tmp.deleteOnExit();
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file for document upload", e);
        }
    }
}
