export const ACCEPTED_DOCUMENT_MIME_TYPES = [
  "application/pdf",
  "image/jpeg",
  "image/png",
] as const;

export type AcceptedDocumentMimeType = (typeof ACCEPTED_DOCUMENT_MIME_TYPES)[number];

export type UploadStatus = "idle" | "uploading" | "success" | "error";

export interface UploadState {
  fileName?: string;
  progress: number;
  status: UploadStatus;
  errorMessage?: string;
}

export interface DocumentUploadResponse {
  documentId: string;
  customerId: string;
  fileName: string;
  contentType: string;
  sizeInBytes: number;
  createdAt: string;
}

export interface DocumentMetadataResponse {
  documentId: string;
  customerId: string;
  fileName: string;
  contentType: string;
  sizeInBytes: number;
}
