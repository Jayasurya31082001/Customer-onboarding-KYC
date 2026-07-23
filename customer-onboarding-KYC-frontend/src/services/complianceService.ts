import { OnboardingStatus, type CustomerResponse } from "../types/customer.types";
import { accountHttpClient, customerHttpClient, riskHttpClient } from "./httpClient";
import { customerService } from "./customerService";
import { documentService } from "./documentService";
import {
  getOnboardingSessionByCustomerId,
  upsertOnboardingSession,
} from "./onboardingSessionService";

export type ComplianceDecision = "APPROVE" | "REJECT" | "REFER";

export interface ComplianceApplication {
  customerId: string;
  firstName?: string;
  lastName?: string;
  email: string;
  dateOfBirth?: string;
  phoneNumber?: string;
  nationality?: string;
  addressLine1?: string;
  city?: string;
  postcode?: string;
  assessedAt: string;
  riskScore: number | null;
  disposition: "AUTO_APPROVE" | "MANUAL_REVIEW" | "AUTO_REJECT";
  status: "OPEN" | "DECIDED";
  documentId: string | null;
}

export interface ComplianceDecisionRequest {
  customerId: string;
  customerEmail?: string;
  decision: ComplianceDecision;
}

const mapCustomersToApplications = async (
  customers: CustomerResponse[],
): Promise<ComplianceApplication[]> => {
  return Promise.all(customers.map(async (customer) => {
    const backendDocumentId = await documentService.getLatestDocumentIdByCustomer(customer.customerId);
    const session = getOnboardingSessionByCustomerId(customer.customerId);

    return {
      documentId: backendDocumentId,
      customerId: customer.customerId,
      firstName: customer.firstName ?? session?.personalDetails?.firstName,
      lastName: customer.lastName ?? session?.personalDetails?.lastName,
      email: customer.email ?? session?.personalDetails?.email,
      dateOfBirth: customer.dateOfBirth ?? session?.personalDetails?.dateOfBirth,
      phoneNumber: customer.phoneNumber ?? session?.personalDetails?.phoneNumber,
      nationality: customer.nationality ?? session?.personalDetails?.nationality,
      addressLine1: customer.addressLine1 ?? session?.personalDetails?.addressLine1,
      city: customer.city ?? session?.personalDetails?.city,
      postcode: customer.postcode ?? session?.personalDetails?.postcode,
      assessedAt: customer.createdAt ?? new Date().toISOString(),
      riskScore: null,
      disposition: "MANUAL_REVIEW",
      status: "OPEN",
    };
  }));
};

interface LatestRiskAssessmentResponse {
  assessmentId?: string;
  customerId?: string;
  score?: number;
  riskScore?: number;
  disposition?: "AUTO_APPROVE" | "MANUAL_REVIEW" | "AUTO_REJECT" | string;
  assessedAt?: string;
  assessedOn?: string;
  createdAt?: string;
}

const riskEndpointsForCustomer = (customerId: string): string[] => {
  return [
    `/api/v1/risk-assessments/customer/${customerId}/latest`,
    `/api/v1/risk-assessments/customer/${customerId}`,
    `/api/v1/risk-assessments/${customerId}/latest`,
    `/api/v1/risk/customer/${customerId}/latest`,
    `/api/v1/risk/customer/${customerId}`,
  ];
};

const unwrapRiskPayload = (payload: unknown): LatestRiskAssessmentResponse | null => {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const source = payload as Record<string, unknown>;
  const nested = source.data;
  if (nested && typeof nested === "object") {
    return nested as LatestRiskAssessmentResponse;
  }

  return source as LatestRiskAssessmentResponse;
};

const normalizeDisposition = (value: string | undefined): ComplianceApplication["disposition"] | null => {
  if (!value) {
    return null;
  }

  const normalized = value.trim().toUpperCase().replace(/[-\s]/g, "_");
  if (normalized === "AUTO_APPROVE") {
    return "AUTO_APPROVE";
  }
  if (normalized === "AUTO_REJECT") {
    return "AUTO_REJECT";
  }
  if (normalized === "MANUAL_REVIEW" || normalized === "MANUAL_APPROVAL_REQUIRED") {
    return "MANUAL_REVIEW";
  }

  return null;
};

const parseRiskResponse = (
  payload: LatestRiskAssessmentResponse,
): Pick<ComplianceApplication, "riskScore" | "disposition" | "assessedAt"> | null => {
  const score = typeof payload.score === "number"
    ? payload.score
    : typeof payload.riskScore === "number"
      ? payload.riskScore
      : null;

  if (score === null) {
    return null;
  }

  const disposition = normalizeDisposition(payload.disposition) ?? "MANUAL_REVIEW";
  const assessedAt = payload.assessedAt ?? payload.assessedOn ?? payload.createdAt ?? new Date().toISOString();

  return {
    riskScore: score,
    disposition,
    assessedAt,
  };
};

const fetchRiskForCustomer = async (customerId: string): Promise<Pick<ComplianceApplication, "riskScore" | "disposition" | "assessedAt"> | null> => {
  const endpoints = riskEndpointsForCustomer(customerId);

  for (const endpoint of endpoints) {
    try {
      const response = await riskHttpClient.get<unknown>(endpoint);
      const unwrapped = unwrapRiskPayload(response.data);
      const parsed = unwrapped ? parseRiskResponse(unwrapped) : null;
      if (parsed) {
        return parsed;
      }
    } catch {
      // Try next candidate endpoint.
    }
  }

  return null;
};

const enrichWithRiskScore = async (applications: ComplianceApplication[]): Promise<ComplianceApplication[]> => {
  const resolved = await Promise.all(
    applications.map(async (application) => {
      const risk = await fetchRiskForCustomer(application.customerId);
      if (risk) {
        return {
          ...application,
          riskScore: risk.riskScore,
          disposition: risk.disposition,
          assessedAt: risk.assessedAt,
        };
      }

      // Keep the record visible but avoid displaying a fake score.
      return application;
    }),
  );

  return resolved;
};

export const complianceService = {
  async getApplications(): Promise<ComplianceApplication[]> {
    const customers = await customerService.getCustomersByStatus(OnboardingStatus.MANUAL_APPROVAL_REQUIRED);
    const mapped = await mapCustomersToApplications(customers);
    return enrichWithRiskScore(mapped);
  },

  async submitDecision(payload: ComplianceDecisionRequest): Promise<void> {
    const session = getOnboardingSessionByCustomerId(payload.customerId);
    const customerEmail = payload.customerEmail ?? session?.personalDetails?.email ?? session?.customerEmail;

    if (!customerEmail) {
      throw new Error("Customer email is required to submit compliance decision.");
    }

    if (payload.decision === "APPROVE") {
      await accountHttpClient.post("/api/internal/events/risk-assessed", {
        customerId: payload.customerId,
        customerEmail,
        disposition: "AUTO_APPROVE",
        score: 75,
      });

      // Ensure customer status leaves MANUAL_APPROVAL_REQUIRED immediately.
      await customerHttpClient.post("/api/internal/events/account-approved", {
        customerId: payload.customerId,
        status: "APPROVED",
      });

      if (session) {
        upsertOnboardingSession(session.customerEmail, session.customerId, {
          applicationStatus: OnboardingStatus.APPROVED,
        });
      }

      return;
    }

    if (payload.decision === "REJECT") {
      await customerHttpClient.post("/api/internal/events/application-rejected", {
        customerId: payload.customerId,
        status: "REJECTED",
      });

      if (session) {
        upsertOnboardingSession(session.customerEmail, session.customerId, {
          applicationStatus: OnboardingStatus.REJECTED,
        });
      }

      return;
    }

    await customerHttpClient.post("/api/internal/events/manual-approval-required", {
      customerId: payload.customerId,
      status: "MANUAL_APPROVAL_REQUIRED",
    });

    if (session) {
      upsertOnboardingSession(session.customerEmail, session.customerId, {
        applicationStatus: OnboardingStatus.MANUAL_APPROVAL_REQUIRED,
      });
    }
  },
};
