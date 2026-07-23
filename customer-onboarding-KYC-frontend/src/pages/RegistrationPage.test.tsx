import { fireEvent, screen, waitFor } from "@testing-library/react";
import { RegistrationPage } from "./RegistrationPage";
import { renderWithProviders } from "../test/renderWithProviders";
import { customerService } from "../services/customerService";
import { OnboardingStatus } from "../types/customer.types";

vi.mock("../services/customerService", () => ({
  customerService: {
    registerCustomer: vi.fn(),
  },
}));

describe("RegistrationPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });
  it("does not show DOB age validation error on initial render", () => {
    renderWithProviders(<RegistrationPage />, { initialRoute: "/onboarding" });

    expect(screen.queryByText(/You must be at least 18 years old/i)).not.toBeInTheDocument();
  });

  it("submits personal details with valid values", async () => {
    vi.mocked(customerService.registerCustomer).mockResolvedValue({
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      status: OnboardingStatus.PENDING,
      createdAt: "2026-07-17T10:00:00",
    });

    renderWithProviders(<RegistrationPage />, { initialRoute: "/onboarding" });

    fireEvent.change(screen.getByLabelText(/First Name/i), { target: { value: "Alice" } });
    fireEvent.change(screen.getByLabelText(/Last Name/i), { target: { value: "Walker" } });
    fireEvent.change(screen.getByLabelText(/^Email$/i), { target: { value: "alice.walker@example.com" } });
    fireEvent.change(screen.getByLabelText(/Date Of Birth/i), { target: { value: "14" } });
    fireEvent.change(screen.getByLabelText(/Month/i), { target: { value: "02" } });
    fireEvent.change(screen.getByLabelText(/Year/i), { target: { value: "1990" } });
    fireEvent.change(screen.getByLabelText(/Phone Number/i), { target: { value: "+447911123456" } });
    fireEvent.change(screen.getByLabelText(/Nationality/i), { target: { value: "GB" } });
    fireEvent.change(screen.getByLabelText(/Address Line 1/i), { target: { value: "221B Baker Street" } });
    fireEvent.change(screen.getByLabelText(/City/i), { target: { value: "London" } });
    fireEvent.change(screen.getByLabelText(/UK Postcode/i), { target: { value: "SW1A 1AA" } });

    fireEvent.click(screen.getByRole("button", { name: /register customer/i }));

    await waitFor(() => {
      expect(customerService.registerCustomer).toHaveBeenCalledTimes(1);
    });

    expect(customerService.registerCustomer).toHaveBeenCalledWith({
      firstName: "Alice",
      lastName: "Walker",
      email: "alice.walker@example.com",
      dateOfBirth: "1990-02-14",
      phoneNumber: "+447911123456",
      nationality: "GB",
      addressLine1: "221B Baker Street",
      city: "London",
      postcode: "SW1A 1AA",
    });
  });

  it("trims leading and trailing spaces before creating customer", async () => {
    vi.mocked(customerService.registerCustomer).mockResolvedValue({
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      status: OnboardingStatus.PENDING,
      createdAt: "2026-07-17T10:00:00",
    });

    renderWithProviders(<RegistrationPage />, { initialRoute: "/onboarding" });

    fireEvent.change(screen.getByLabelText(/First Name/i), { target: { value: "  Alice  " } });
    fireEvent.change(screen.getByLabelText(/Last Name/i), { target: { value: "  Walker  " } });
    fireEvent.change(screen.getByLabelText(/^Email$/i), { target: { value: "  ALICE.WALKER@EXAMPLE.COM  " } });
    fireEvent.change(screen.getByLabelText(/Date Of Birth/i), { target: { value: "14" } });
    fireEvent.change(screen.getByLabelText(/Month/i), { target: { value: "02" } });
    fireEvent.change(screen.getByLabelText(/Year/i), { target: { value: "1990" } });
    fireEvent.change(screen.getByLabelText(/Phone Number/i), { target: { value: "  +447911123456  " } });
    fireEvent.change(screen.getByLabelText(/Nationality/i), { target: { value: " gb " } });
    fireEvent.change(screen.getByLabelText(/Address Line 1/i), { target: { value: "  221B Baker Street  " } });
    fireEvent.change(screen.getByLabelText(/City/i), { target: { value: "  London  " } });
    fireEvent.change(screen.getByLabelText(/UK Postcode/i), { target: { value: "  sw1a 1aa  " } });

    fireEvent.click(screen.getByRole("button", { name: /register customer/i }));

    await waitFor(() => {
      expect(customerService.registerCustomer).toHaveBeenCalledTimes(1);
    });

    expect(customerService.registerCustomer).toHaveBeenCalledWith({
      firstName: "Alice",
      lastName: "Walker",
      email: "alice.walker@example.com",
      dateOfBirth: "1990-02-14",
      phoneNumber: "+447911123456",
      nationality: "GB",
      addressLine1: "221B Baker Street",
      city: "London",
      postcode: "SW1A 1AA",
    });
  });

  it("submits personal details when names contain spaces, hyphens, or apostrophes", async () => {
    vi.mocked(customerService.registerCustomer).mockResolvedValue({
      customerId: "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
      status: OnboardingStatus.PENDING,
      createdAt: "2026-07-17T10:00:00",
    });

    renderWithProviders(<RegistrationPage />, { initialRoute: "/onboarding" });

    fireEvent.change(screen.getByLabelText(/First Name/i), { target: { value: "Mary-Jane" } });
    fireEvent.change(screen.getByLabelText(/Last Name/i), { target: { value: "O'Connor" } });
    fireEvent.change(screen.getByLabelText(/^Email$/i), { target: { value: "mary.oconnor@example.com" } });
    fireEvent.change(screen.getByLabelText(/Date Of Birth/i), { target: { value: "14" } });
    fireEvent.change(screen.getByLabelText(/Month/i), { target: { value: "02" } });
    fireEvent.change(screen.getByLabelText(/Year/i), { target: { value: "1990" } });
    fireEvent.change(screen.getByLabelText(/Phone Number/i), { target: { value: "+447911123456" } });
    fireEvent.change(screen.getByLabelText(/Nationality/i), { target: { value: "GB" } });
    fireEvent.change(screen.getByLabelText(/Address Line 1/i), { target: { value: "221B Baker Street" } });
    fireEvent.change(screen.getByLabelText(/City/i), { target: { value: "London" } });
    fireEvent.change(screen.getByLabelText(/UK Postcode/i), { target: { value: "SW1A 1AA" } });

    fireEvent.click(screen.getByRole("button", { name: /register customer/i }));

    await waitFor(() => {
      expect(customerService.registerCustomer).toHaveBeenCalledTimes(1);
    });

    expect(customerService.registerCustomer).toHaveBeenCalledWith({
      firstName: "Mary-Jane",
      lastName: "O'Connor",
      email: "mary.oconnor@example.com",
      dateOfBirth: "1990-02-14",
      phoneNumber: "+447911123456",
      nationality: "GB",
      addressLine1: "221B Baker Street",
      city: "London",
      postcode: "SW1A 1AA",
    });
  });
});
