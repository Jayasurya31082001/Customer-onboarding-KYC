import { fireEvent, screen, waitFor } from "@testing-library/react";
import { DocumentUploadPage } from "./DocumentUploadPage";
import { renderWithProviders } from "../test/renderWithProviders";
import { documentService } from "../services/documentService";

vi.mock("../services/documentService", () => ({
  documentService: {
    uploadDocument: vi.fn(),
  },
}));

vi.mock("../hooks/useOnboarding", () => ({
  useOnboarding: () => ({
    state: { customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd" },
    dispatch: vi.fn(),
  }),
}));

describe("DocumentUploadPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("displays error message when a file with invalid type is selected", async () => {
    renderWithProviders(<DocumentUploadPage />);

    const invalidFile = new File(["dummy content"], "test.txt", { type: "text/plain" });
    const inputElement = document.querySelector('input[type="file"]') as HTMLInputElement;

    fireEvent.change(inputElement, { target: { files: [invalidFile] } });

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        /File "test.txt" has an unsupported file type/i,
      );
    });
  });

  it("displays error message when a file exceeding maximum size is selected", async () => {
    renderWithProviders(<DocumentUploadPage />);

    const oversizedFile = new File([new ArrayBuffer(6 * 1024 * 1024)], "large.pdf", {
      type: "application/pdf",
    });
    const inputElement = document.querySelector('input[type="file"]') as HTMLInputElement;

    fireEvent.change(inputElement, { target: { files: [oversizedFile] } });

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        /File "large.pdf" exceeds the maximum allowed size of 5 MB/i,
      );
    });
  });

  it("uploads a valid file successfully", async () => {
    vi.mocked(documentService.uploadDocument).mockResolvedValue({
      documentId: "doc-123",
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      fileName: "passport.pdf",
      contentType: "application/pdf",
      sizeInBytes: 1024,
      createdAt: "2026-07-23T12:00:00Z",
    });

    renderWithProviders(<DocumentUploadPage />);

    const validFile = new File(["pdf data"], "passport.pdf", { type: "application/pdf" });
    const inputElement = document.querySelector('input[type="file"]') as HTMLInputElement;

    fireEvent.change(inputElement, { target: { files: [validFile] } });

    await waitFor(() => {
      expect(documentService.uploadDocument).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("status")).toHaveTextContent(/Document uploaded successfully/i);
    });
  });
});
