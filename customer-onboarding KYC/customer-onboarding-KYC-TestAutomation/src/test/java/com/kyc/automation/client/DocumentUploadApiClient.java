package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;

/**
 * API client for the Document Service (port 8082).
 */
public class DocumentUploadApiClient {

    private final RequestSpecification spec;

    public DocumentUploadApiClient() {
        this.spec = BaseApiConfig.documentServiceSpec();
    }

    public Response uploadDocument(String customerId, File file, String mimeType) {
        return given()
                .spec(spec)
                .queryParam("customerId", customerId)
                .multiPart("file", file, mimeType)
                .when()
                .post("/api/documents");
    }

    public Response uploadDocumentBytes(String customerId,
                                        byte[] content,
                                        String fileName,
                                        String mimeType) {
        File tmp = createTempFile(content, fileName);
        try {
            return uploadDocument(customerId, tmp, mimeType);
        } finally {
            tmp.delete();
        }
    }

    public Response downloadDocument(String documentId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/documents/{documentId}", documentId);
    }

    public Response getDocumentMetadata(String documentId) {
        return given()
                .spec(spec)
                .accept("application/json")
                .when()
                .get("/api/documents/metadata/{documentId}", documentId);
    }

    public Response getLatestDocumentMetadataByCustomer(String customerId) {
        return given()
                .spec(spec)
                .accept("application/json")
                .when()
                .get("/api/documents/customer/{customerId}/latest", customerId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** %PDF-1.4 magic bytes */
    public static byte[] minimalPdfContent() {
        return new byte[]{ 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34, 0x0A };
    }

    /** JPEG magic bytes (FF D8 FF) */
    public static byte[] minimalJpegContent() {
        return new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00};
    }

    /** PNG magic bytes (89 50 4E 47 0D 0A 1A 0A) */
    public static byte[] minimalPngContent() {
        return new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    /** Generates a byte array larger than 5 MB (starts with PDF magic bytes so magic check passes, but size check fails). */
    public static byte[] oversizedContent() {
        byte[] bytes = new byte[5 * 1024 * 1024 + 1];
        bytes[0] = 0x25; // %
        bytes[1] = 0x50; // P
        bytes[2] = 0x44; // D
        bytes[3] = 0x46; // F
        return bytes;
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
