import type { AxiosProgressEvent } from "axios";
import type { DocumentUploadResponse } from "../types/document.types";
import { documentHttpClient } from "./httpClient";
import type { DocumentMetadataResponse } from "../types/document.types";

export const documentService = {
  async uploadDocument(
    customerId: string,
    file: File,
    onProgress?: (progress: number) => void,
  ): Promise<DocumentUploadResponse> {
    const formData = new FormData();
    formData.append("customerId", customerId);
    formData.append("file", file);

    const response = await documentHttpClient.post<DocumentUploadResponse>("/api/documents", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      onUploadProgress: (event: AxiosProgressEvent) => {
        if (!event.total || !onProgress) {
          return;
        }
        const progress = Math.round((event.loaded / event.total) * 100);
        onProgress(progress);
      },
    });

    return response.data;
  },

  async downloadDocument(documentId: string): Promise<{ blob: Blob; fileName: string }> {
    const response = await documentHttpClient.get<Blob>(`/api/documents/${documentId}`, {
      responseType: "blob",
    });

    const dispositionHeader = response.headers["content-disposition"] as string | undefined;
    const fileNameMatch = dispositionHeader?.match(/filename="?([^\";]+)"?/i);
    const fallbackExtension = response.data.type === "application/pdf" ? "pdf" : "bin";
    const fallbackName = `document-${documentId}.${fallbackExtension}`;

    return {
      blob: response.data,
      fileName: fileNameMatch?.[1] ?? fallbackName,
    };
  },

  async downloadLatestDocumentByCustomer(customerId: string): Promise<{ blob: Blob; fileName: string }> {
    const response = await documentHttpClient.get<Blob>(`/api/documents/customer/${customerId}/latest/download`, {
      responseType: "blob",
    });

    const dispositionHeader = response.headers["content-disposition"] as string | undefined;
    const fileNameMatch = dispositionHeader?.match(/filename="?([^\";]+)"?/i);
    const fallbackExtension = response.data.type === "application/pdf" ? "pdf" : "bin";
    const fallbackName = `latest-document-${customerId}.${fallbackExtension}`;

    return {
      blob: response.data,
      fileName: fileNameMatch?.[1] ?? fallbackName,
    };
  },

  async getLatestDocumentIdByCustomer(customerId: string): Promise<string | null> {
    try {
      const response = await documentHttpClient.get<DocumentMetadataResponse>(
        `/api/documents/customer/${customerId}/latest`,
      );
      return response.data.documentId;
    } catch {
      return null;
    }
  },
};
