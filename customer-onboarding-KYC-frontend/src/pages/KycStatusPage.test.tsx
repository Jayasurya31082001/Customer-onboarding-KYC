import { describe, expect, it } from "vitest";
import { normalizeKycStatus } from "./KycStatusPage";
import { KycStatus } from "../types/onboarding.types";

describe("normalizeKycStatus", () => {
  it("maps failure variants to FAIL", () => {
    expect(normalizeKycStatus("FAIL")).toBe(KycStatus.FAIL);
    expect(normalizeKycStatus("failed")).toBe(KycStatus.FAIL);
    expect(normalizeKycStatus("KYC_FAILED")).toBe(KycStatus.FAIL);
    expect(normalizeKycStatus("kyc-fail")).toBe(KycStatus.FAIL);
  });

  it("maps pass variants to PASS", () => {
    expect(normalizeKycStatus("PASS")).toBe(KycStatus.PASS);
    expect(normalizeKycStatus("KYC_PASSED")).toBe(KycStatus.PASS);
    expect(normalizeKycStatus("KYC_COMPLETED")).toBe(KycStatus.PASS);
    expect(normalizeKycStatus("APPROVED")).toBe(KycStatus.PASS);
  });

  it("maps rejected variant to FAIL", () => {
    expect(normalizeKycStatus("REJECTED")).toBe(KycStatus.FAIL);
  });

  it("defaults unknown statuses to in-progress", () => {
    expect(normalizeKycStatus("PENDING_REVIEW")).toBe(KycStatus.KYC_IN_PROGRESS);
  });
});
