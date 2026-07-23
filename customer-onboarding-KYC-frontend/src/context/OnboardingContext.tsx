import { createContext, useEffect, useMemo, useReducer, type Dispatch, type ReactNode } from "react";
import type { OnboardingAction, OnboardingState } from "../types/onboarding.types";
import { OnboardingStep } from "../types/onboarding.types";
import { getActiveOnboardingEmail, hydrateOnboardingStateForEmail, upsertOnboardingSession } from "../services/onboardingSessionService";

const defaultOnboardingState: OnboardingState = {
  currentStep: OnboardingStep.PERSONAL_DETAILS,
  customerId: null,
  personalDetails: null,
  documentId: null,
  documentsUploaded: false,
  kycStatus: null,
  applicationStatus: null,
  correlationId: null,
};

const getInitialOnboardingState = (): OnboardingState => {
  const activeEmail = getActiveOnboardingEmail();

  if (!activeEmail) {
    return defaultOnboardingState;
  }

  return hydrateOnboardingStateForEmail(activeEmail) ?? defaultOnboardingState;
};

const onboardingReducer = (state: OnboardingState, action: OnboardingAction): OnboardingState => {
  switch (action.type) {
    case "RESET":
      return defaultOnboardingState;
    case "HYDRATE":
      return action.payload;
    case "SET_STEP":
      return {
        ...state,
        currentStep: action.payload,
      };
    case "RETRY_DOCUMENT_UPLOAD":
      return {
        ...state,
        documentId: null,
        documentsUploaded: false,
        kycStatus: null,
        correlationId: null,
        currentStep: OnboardingStep.DOCUMENT_UPLOAD,
      };
    case "SET_PERSONAL_DETAILS":
      return {
        ...state,
        customerId: action.payload.customerId,
        personalDetails: action.payload.details,
        applicationStatus: action.payload.status,
        currentStep: OnboardingStep.DOCUMENT_UPLOAD,
      };
    case "SET_DOCUMENT_UPLOADED":
      return {
        ...state,
        documentId: action.payload.documentId,
        documentsUploaded: true,
        kycStatus: null,
        correlationId: null,
        currentStep: OnboardingStep.KYC_VERIFICATION,
      };
    case "SET_KYC_STATUS": {
      return {
        ...state,
        kycStatus: action.payload.status,
        correlationId: action.payload.correlationId ?? state.correlationId,
        currentStep: OnboardingStep.KYC_VERIFICATION,
      };
    }
    case "SET_APPLICATION_STATUS":
      return {
        ...state,
        applicationStatus: action.payload,
        currentStep: OnboardingStep.CONFIRMATION,
      };
    default:
      return state;
  }
};

export interface OnboardingContextValue {
  state: OnboardingState;
  dispatch: Dispatch<OnboardingAction>;
}

export const OnboardingContext = createContext<OnboardingContextValue | undefined>(undefined);

interface OnboardingProviderProps {
  children: ReactNode;
}

export const OnboardingProvider = ({ children }: OnboardingProviderProps) => {
  const [state, dispatch] = useReducer(onboardingReducer, undefined, getInitialOnboardingState);

  useEffect(() => {
    if (!state.personalDetails?.email || !state.customerId) {
      return;
    }

    upsertOnboardingSession(state.personalDetails.email, state.customerId, {
      currentStep: state.currentStep,
      personalDetails: state.personalDetails,
      documentId: state.documentId,
      documentsUploaded: state.documentsUploaded,
      kycStatus: state.kycStatus,
      applicationStatus: state.applicationStatus,
      correlationId: state.correlationId,
    });
  }, [state]);

  const value = useMemo<OnboardingContextValue>(
    () => ({
      state,
      dispatch,
    }),
    [state],
  );

  return <OnboardingContext.Provider value={value}>{children}</OnboardingContext.Provider>;
};
