import { fireEvent, screen, waitFor } from "@testing-library/react";
import App from "../App";
import { registerCustomerEmail } from "../services/authService";
import { upsertOnboardingSession } from "../services/onboardingSessionService";
import { renderWithProviders } from "../test/renderWithProviders";
import { OnboardingStatus } from "../types/customer.types";
import { OnboardingStep } from "../types/onboarding.types";

describe("LoginPage", () => {
  it("logs in with valid email and fixed password", async () => {
    registerCustomerEmail("customer@example.com");
    upsertOnboardingSession("customer@example.com", "cust-123", {
      currentStep: OnboardingStep.DOCUMENT_UPLOAD,
      personalDetails: {
        firstName: "Alice",
        lastName: "Walker",
        email: "customer@example.com",
        dateOfBirth: "1990-02-14",
        phoneNumber: "+447911123456",
        nationality: "GB",
        addressLine1: "221B Baker Street",
        city: "London",
        postcode: "SW1A 1AA",
      },
      documentId: null,
      documentsUploaded: false,
      kycStatus: null,
      applicationStatus: OnboardingStatus.PENDING,
      correlationId: null,
    });

    renderWithProviders(<App />, { initialRoute: "/login" });

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "customer@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/Password/i), {
      target: { value: "Welcome@123" },
    });

    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/Step 2: Document Upload/i)).toBeInTheDocument();
    });
  });

  it("accepts email and password with surrounding spaces and mixed-case email", async () => {
    registerCustomerEmail("customer@example.com");

    renderWithProviders(<App />, { initialRoute: "/login" });

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "  Customer@Example.com  " },
    });
    fireEvent.change(screen.getByLabelText(/Password/i), {
      target: { value: "  Welcome@123  " },
    });

    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/Step 1: Personal Details/i)).toBeInTheDocument();
    });
  });

  it("resets onboarding after logout when a different user logs in", async () => {
    registerCustomerEmail("first.customer@example.com");
    upsertOnboardingSession("first.customer@example.com", "cust-first", {
      currentStep: OnboardingStep.DOCUMENT_UPLOAD,
      personalDetails: {
        firstName: "First",
        lastName: "Customer",
        email: "first.customer@example.com",
        dateOfBirth: "1990-02-14",
        phoneNumber: "+447911123456",
        nationality: "GB",
        addressLine1: "221B Baker Street",
        city: "London",
        postcode: "SW1A 1AA",
      },
      documentId: null,
      documentsUploaded: false,
      kycStatus: null,
      applicationStatus: OnboardingStatus.PENDING,
      correlationId: null,
    });

    registerCustomerEmail("second.customer@example.com");

    renderWithProviders(<App />, { initialRoute: "/login" });

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "first.customer@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/Password/i), {
      target: { value: "Welcome@123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/Step 2: Document Upload/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: /log out/i }));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "second.customer@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/Password/i), {
      target: { value: "Welcome@123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/Step 1: Personal Details/i)).toBeInTheDocument();
    });
  });
});
