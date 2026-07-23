import type { KycStatusResponse } from "../types/onboarding.types";
import { KycStatus } from "../types/onboarding.types";
import { kycHttpClient } from "./httpClient";

// TODO(BACKEND_NOT_FOUND): No public KYC status endpoint exists in kyc-service controllers.
// Expected from UI guide: polling endpoint for customer KYC status.
const kycStatusEndpointTemplate = import.meta.env.VITE_KYC_STATUS_ENDPOINT_TEMPLATE as string | undefined;
const isDemoModeEnabled = String(import.meta.env.VITE_KYC_DEMO_MODE ?? "false").toLowerCase() === "true";

const demoPollCountBySession = new Map<string, number>();

const buildDemoSessionKey = (customerId: string, documentId?: string): string => {
  const normalizedCustomerId = customerId.trim();
  const normalizedDocumentId = documentId?.trim() ?? "no-document";
  return `${normalizedCustomerId}::${normalizedDocumentId}`;
};

const pickDemoOutcome = (sessionKey: string): KycStatus => {
  const lastChar = sessionKey.at(-1)?.toLowerCase();

  if (lastChar === "a" || lastChar === "b") {
    return KycStatus.REFER;
  }

  if (lastChar === "f") {
    return KycStatus.FAIL;
  }

  return KycStatus.PASS;
};

const getDemoKycStatus = async (customerId: string, documentId?: string): Promise<KycStatusResponse> => {
  const sessionKey = buildDemoSessionKey(customerId, documentId);
  const attempts = (demoPollCountBySession.get(sessionKey) ?? 0) + 1;
  demoPollCountBySession.set(sessionKey, attempts);

  const finalStatus = pickDemoOutcome(sessionKey);
  const status = attempts >= 3 ? finalStatus : KycStatus.KYC_IN_PROGRESS;

  return {
    status,
    correlationId: `demo-kyc-${sessionKey}`,
  };
};

const readStatusField = (payload: unknown): string | null => {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const source = payload as Record<string, unknown>;
  const candidates = [source.status, source.kycStatus, source.result, source.decision];

  for (const candidate of candidates) {
    if (typeof candidate === "string" && candidate.trim().length > 0) {
      return candidate;
    }
  }

  return null;
};

const readCorrelationIdField = (payload: unknown, customerId: string): string => {
  if (payload && typeof payload === "object") {
    const source = payload as Record<string, unknown>;
    if (typeof source.correlationId === "string" && source.correlationId.trim().length > 0) {
      return source.correlationId;
    }
  }

  return `kyc-correlation-missing-${customerId}`;
};

export const kycService = {
  async getKycStatus(customerId: string, documentId?: string): Promise<KycStatusResponse> {
    if (!kycStatusEndpointTemplate) {
      if (isDemoModeEnabled) {
        return getDemoKycStatus(customerId, documentId);
      }

      return {
        status: KycStatus.KYC_IN_PROGRESS,
        correlationId: `kyc-endpoint-not-configured-${customerId}`,
      };
    }

    try {
      const endpointWithCustomer = kycStatusEndpointTemplate.replace(":customerId", customerId);
      const endpointWithDocument = documentId
        ? endpointWithCustomer.replace(":documentId", encodeURIComponent(documentId))
        : endpointWithCustomer;
      const response = await kycHttpClient.get<KycStatusResponse>(endpointWithDocument);
      const status = readStatusField(response.data);

      if (!status) {
        return {
          status: KycStatus.FAIL,
          correlationId: `kyc-status-missing-${customerId}`,
        };
      }

      return {
        status,
        correlationId: readCorrelationIdField(response.data, customerId),
      };
    } catch (error) {
      // Keep polling in-progress on transient fetch issues so UI can recover to latest backend status.
      return {
        status: KycStatus.KYC_IN_PROGRESS,
        correlationId: `kyc-fetch-failed-${customerId}`,
      };
    }
  },
};
