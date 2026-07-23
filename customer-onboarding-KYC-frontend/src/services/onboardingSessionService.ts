import type { PersonalDetailsForm } from "../types/customer.types";
import { OnboardingStatus } from "../types/customer.types";
import { KycStatus, OnboardingStep, type OnboardingState } from "../types/onboarding.types";

const ACTIVE_ONBOARDING_EMAIL_KEY = "kyc.onboarding.active.email";
const ONBOARDING_SESSIONS_KEY = "kyc.onboarding.sessions";

export interface StoredOnboardingSession {
  customerId: string;
  customerEmail: string;
  currentStep: OnboardingStep;
  personalDetails: PersonalDetailsForm | null;
  documentId: string | null;
  documentsUploaded: boolean;
  kycStatus: KycStatus | null;
  applicationStatus: OnboardingStatus | null;
  correlationId: string | null;
}

const canUseStorage = (): boolean => {
  if (typeof window === "undefined") {
    return false;
  }

  try {
    return typeof window.localStorage !== "undefined";
  } catch {
    return false;
  }
};

let inMemoryActiveEmail: string | null = null;
let inMemorySessions: Record<string, StoredOnboardingSession> = {};

const getSessions = (): Record<string, StoredOnboardingSession> => {
  if (!canUseStorage()) {
    return inMemorySessions;
  }

  const raw = window.localStorage.getItem(ONBOARDING_SESSIONS_KEY);
  if (!raw) {
    return {};
  }

  try {
    const parsed = JSON.parse(raw) as unknown;
    inMemorySessions = parsed && typeof parsed === "object" ? (parsed as Record<string, StoredOnboardingSession>) : {};
    return inMemorySessions;
  } catch {
    return {};
  }
};

const setSessions = (sessions: Record<string, StoredOnboardingSession>): void => {
  inMemorySessions = sessions;

  if (!canUseStorage()) {
    return;
  }

  window.localStorage.setItem(ONBOARDING_SESSIONS_KEY, JSON.stringify(sessions));
};

export const setActiveOnboardingEmail = (email: string): void => {
  inMemoryActiveEmail = email.trim().toLowerCase();

  if (!canUseStorage()) {
    return;
  }

  window.localStorage.setItem(ACTIVE_ONBOARDING_EMAIL_KEY, inMemoryActiveEmail);
};

export const getActiveOnboardingEmail = (): string | null => {
  if (!canUseStorage()) {
    return inMemoryActiveEmail;
  }

  const stored = window.localStorage.getItem(ACTIVE_ONBOARDING_EMAIL_KEY);
  inMemoryActiveEmail = stored;
  return stored;
};

export const clearActiveOnboardingEmail = (): void => {
  inMemoryActiveEmail = null;

  if (!canUseStorage()) {
    return;
  }

  window.localStorage.removeItem(ACTIVE_ONBOARDING_EMAIL_KEY);
};

export const upsertOnboardingSession = (
  customerEmail: string,
  customerId: string,
  partialState: Partial<StoredOnboardingSession>,
): void => {
  const normalizedEmail = customerEmail.trim().toLowerCase();
  const sessions = getSessions();
  sessions[normalizedEmail] = {
    customerEmail: normalizedEmail,
    customerId,
    currentStep: partialState.currentStep ?? sessions[normalizedEmail]?.currentStep ?? OnboardingStep.PERSONAL_DETAILS,
    personalDetails: partialState.personalDetails ?? sessions[normalizedEmail]?.personalDetails ?? null,
    documentId: partialState.documentId ?? sessions[normalizedEmail]?.documentId ?? null,
    documentsUploaded: partialState.documentsUploaded ?? sessions[normalizedEmail]?.documentsUploaded ?? false,
    kycStatus: partialState.kycStatus ?? sessions[normalizedEmail]?.kycStatus ?? null,
    applicationStatus: partialState.applicationStatus ?? sessions[normalizedEmail]?.applicationStatus ?? null,
    correlationId: partialState.correlationId ?? sessions[normalizedEmail]?.correlationId ?? null,
  };
  setSessions(sessions);
  setActiveOnboardingEmail(normalizedEmail);
};

export const getOnboardingSessionByEmail = (email: string): StoredOnboardingSession | null => {
  const sessions = getSessions();
  return sessions[email.trim().toLowerCase()] ?? null;
};

export const getOnboardingSessionByCustomerId = (customerId: string): StoredOnboardingSession | null => {
  const normalizedCustomerId = customerId.trim();
  if (!normalizedCustomerId) {
    return null;
  }

  const sessions = getSessions();
  const allSessions = Object.values(sessions);
  return allSessions.find((session) => session.customerId === normalizedCustomerId) ?? null;
};

export const getAllOnboardingSessions = (): StoredOnboardingSession[] => {
  return Object.values(getSessions());
};

export const clearOnboardingSessionForActiveEmail = (): void => {
  const activeEmail = getActiveOnboardingEmail();
  if (!activeEmail) {
    return;
  }

  const sessions = getSessions();
  delete sessions[activeEmail];
  setSessions(sessions);
  clearActiveOnboardingEmail();
};

export const resetOnboardingSession = (email: string): void => {
  const sessions = getSessions();
  delete sessions[email.trim().toLowerCase()];
  setSessions(sessions);
};

export const resetOnboardingSessionState = (): void => {
  inMemoryActiveEmail = null;
  inMemorySessions = {};

  if (!canUseStorage()) {
    return;
  }

  window.localStorage.removeItem(ACTIVE_ONBOARDING_EMAIL_KEY);
  window.localStorage.removeItem(ONBOARDING_SESSIONS_KEY);
};

export const resolveOnboardingRouteForEmail = (email: string): string => {
  const session = getOnboardingSessionByEmail(email);

  if (!session) {
    return "/onboarding";
  }

  switch (session.currentStep) {
    case OnboardingStep.DOCUMENT_UPLOAD:
      return "/onboarding";
    case OnboardingStep.KYC_VERIFICATION:
      return "/onboarding";
    case OnboardingStep.REVIEW:
      return "/onboarding";
    case OnboardingStep.CONFIRMATION:
      return "/onboarding/confirmation";
    default:
      return "/onboarding";
  }
};

export const hydrateOnboardingStateForEmail = (email: string): OnboardingState | null => {
  const session = getOnboardingSessionByEmail(email);
  if (!session) {
    return null;
  }

  return {
    currentStep: session.currentStep,
    customerId: session.customerId,
    personalDetails: session.personalDetails,
    documentId: session.documentId,
    documentsUploaded: session.documentsUploaded,
    kycStatus: session.kycStatus,
    applicationStatus: session.applicationStatus,
    correlationId: session.correlationId,
  };
};
