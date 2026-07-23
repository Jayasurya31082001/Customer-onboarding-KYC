import { useCallback, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useDropzone, type FileRejection } from "react-dropzone";
import { useOnboarding } from "../hooks/useOnboarding";
import { documentService } from "../services/documentService";
import {
  ACCEPTED_DOCUMENT_MIME_TYPES,
  type AcceptedDocumentMimeType,
  type UploadState,
} from "../types/document.types";

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

const acceptedMimeLookup: Record<AcceptedDocumentMimeType, string[]> = {
  "application/pdf": [".pdf"],
  "image/jpeg": [".jpg", ".jpeg"],
  "image/png": [".png"],
};

export const DocumentUploadPage = () => {
  const queryClient = useQueryClient();
  const {
    state: { customerId },
    dispatch,
  } = useOnboarding();

  const [uploadState, setUploadState] = useState<UploadState>({
    progress: 0,
    status: "idle",
  });

  const [announceMessage, setAnnounceMessage] = useState("No uploads in progress.");

  const handleUpload = useCallback(
    async (file: File) => {
      if (!customerId) {
        setUploadState({
          status: "error",
          progress: 0,
          errorMessage: "Customer ID is required before upload.",
        });
        return;
      }

      setUploadState({
        fileName: file.name,
        progress: 0,
        status: "uploading",
      });
      setAnnounceMessage(`Uploading ${file.name}`);

      try {
        const response = await documentService.uploadDocument(customerId, file, (progress) => {
          setUploadState((previous) => ({
            ...previous,
            progress,
            status: "uploading",
          }));
          setAnnounceMessage(`Upload progress ${progress}%`);
        });

        setUploadState({
          fileName: response.fileName,
          progress: 100,
          status: "success",
        });
        setAnnounceMessage(`Upload complete for ${response.fileName}`);

        queryClient.removeQueries({
          queryKey: ["kyc-status"],
        });

        dispatch({
          type: "SET_DOCUMENT_UPLOADED",
          payload: {
            documentId: response.documentId,
          },
        });
      } catch (error) {
        const message = error instanceof Error ? error.message : "Document upload failed";
        setUploadState({
          fileName: file.name,
          progress: 0,
          status: "error",
          errorMessage: message,
        });
        setAnnounceMessage(message);
      }
    },
    [customerId, dispatch, queryClient],
  );

  const onDrop = useCallback(
    (acceptedFiles: File[], fileRejections: FileRejection[]) => {
      if (fileRejections.length > 0) {
        const rejection = fileRejections[0];
        const error = rejection.errors[0];
        let errorMessage = `File "${rejection.file.name}" was rejected.`;

        if (error?.code === "file-too-large") {
          errorMessage = `File "${rejection.file.name}" exceeds the maximum allowed size of 5 MB.`;
        } else if (error?.code === "file-invalid-type") {
          errorMessage = `File "${rejection.file.name}" has an unsupported file type. Accepted formats: PDF, JPEG, PNG.`;
        } else if (error?.message) {
          errorMessage = error.message;
        }

        setUploadState({
          fileName: rejection.file.name,
          progress: 0,
          status: "error",
          errorMessage,
        });
        setAnnounceMessage(errorMessage);
        return;
      }

      const selected = acceptedFiles[0];
      if (!selected) {
        return;
      }
      void handleUpload(selected);
    },
    [handleUpload],
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    maxSize: MAX_FILE_SIZE_BYTES,
    accept: acceptedMimeLookup,
  });

  return (
    <section aria-labelledby="document-upload-heading" data-e2e="document-upload-page">
      <h2 id="document-upload-heading" className="text-2xl font-bold text-slate-900">
        Step 2: Document Upload
      </h2>
      <p className="mt-1 text-sm text-slate-700">Upload a PDF, JPEG, or PNG document up to 5 MB.</p>

      <div
        {...getRootProps()}
        className={[
          "mt-5 cursor-pointer rounded-xl border-2 border-dashed p-8 text-center",
          isDragActive ? "border-sky-500 bg-sky-50" : "border-slate-300 bg-slate-50",
        ].join(" ")}
        data-e2e="document-dropzone"
      >
        <input {...getInputProps()} />
        <p className="font-semibold text-slate-900">Drag and drop your file here, or click to select.</p>
        <p className="mt-1 text-sm text-slate-700">Accepted: PDF/JPEG/PNG. Max 5 MB.</p>
      </div>

      <div className="mt-4" aria-live="polite" data-e2e="upload-status-live">
        <p className="text-sm text-slate-800">{announceMessage}</p>
      </div>

      {uploadState.status === "uploading" ? (
        <div className="mt-4">
          <div className="mb-1 flex justify-between text-sm">
            <span>{uploadState.fileName}</span>
            <span>{uploadState.progress}%</span>
          </div>
          <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
            <div className="h-full bg-sky-500" style={{ width: `${uploadState.progress}%` }} />
          </div>
        </div>
      ) : null}

      {uploadState.status === "success" ? (
        <p className="mt-4 rounded bg-emerald-50 p-3 text-sm text-emerald-800" role="status">
          Document uploaded successfully. Moving to KYC verification.
        </p>
      ) : null}

      {uploadState.status === "error" ? (
        <p className="mt-4 rounded bg-red-50 p-3 text-sm text-red-700" role="alert">
          {uploadState.errorMessage}
        </p>
      ) : null}

      <p className="mt-4 text-xs text-slate-700">Customer ID: {customerId ?? "Not available yet"}</p>
      <p className="mt-1 text-xs text-slate-700">
        File validation: types {ACCEPTED_DOCUMENT_MIME_TYPES.join(", ")} and max {(MAX_FILE_SIZE_BYTES / (1024 * 1024)).toFixed(0)} MB.
      </p>
    </section>
  );
};
