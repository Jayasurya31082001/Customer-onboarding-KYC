import { screen, waitFor } from "@testing-library/react";
import { AccountConfirmationPage } from "./AccountConfirmationPage";
import { renderWithProviders } from "../test/renderWithProviders";
import { customerService } from "../services/customerService";
import { accountService } from "../services/accountService";
import { OnboardingStatus } from "../types/customer.types";

vi.mock("../services/customerService", () => ({
  customerService: {
    getCustomer: vi.fn(),
  },
}));

vi.mock("../services/accountService", () => ({
  accountService: {
    getAccountByCustomer: vi.fn(),
  },
}));

vi.mock("../hooks/useOnboarding", () => ({
  useOnboarding: () => ({
    state: { customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd" },
    dispatch: vi.fn(),
  }),
}));

describe("AccountConfirmationPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders rejection message when application status is REJECTED", async () => {
    vi.mocked(customerService.getCustomer).mockResolvedValue({
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      firstName: "Alice",
      lastName: "Walker",
      email: "alice.walker@example.com",
      dateOfBirth: "1990-02-14",
      phoneNumber: "+447911123456",
      nationality: "GB",
      addressLine1: "221B Baker Street",
      city: "London",
      postcode: "SW1A 1AA",
      status: OnboardingStatus.REJECTED,
      createdAt: "2026-07-23T10:00:00Z",
    });

    renderWithProviders(<AccountConfirmationPage />);

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(/Application got rejected/i);
    });
  });

  it("renders approved account details when account is created", async () => {
    vi.mocked(customerService.getCustomer).mockResolvedValue({
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      firstName: "Alice",
      lastName: "Walker",
      email: "alice.walker@example.com",
      dateOfBirth: "1990-02-14",
      phoneNumber: "+447911123456",
      nationality: "GB",
      addressLine1: "221B Baker Street",
      city: "London",
      postcode: "SW1A 1AA",
      status: OnboardingStatus.APPROVED,
      createdAt: "2026-07-23T10:00:00Z",
    });

    vi.mocked(accountService.getAccountByCustomer).mockResolvedValue({
      accountId: "acc-101",
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      accountNumber: "12345678",
      sortCode: "10-20-30",
      accountType: "SAVINGS",
      status: "ACTIVE",
      createdAt: "2026-07-23T10:00:00Z",
    });

    renderWithProviders(<AccountConfirmationPage />);

    await waitFor(() => {
      expect(screen.getByText(/Application approved and account created/i)).toBeInTheDocument();
      expect(screen.getByText("12345678")).toBeInTheDocument();
      expect(screen.getByText("10-20-30")).toBeInTheDocument();
    });
  });
});
