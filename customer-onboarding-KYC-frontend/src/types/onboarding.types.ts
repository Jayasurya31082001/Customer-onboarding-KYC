import type { PersonalDetailsForm } from "./customer.types";
import { OnboardingStatus } from "./customer.types";

export enum OnboardingStep {
  PERSONAL_DETAILS = "PERSONAL_DETAILS",
  DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD",
  KYC_VERIFICATION = "KYC_VERIFICATION",
  REVIEW = "REVIEW",
  CONFIRMATION = "CONFIRMATION",
}

export enum KycStatus {
  KYC_IN_PROGRESS = "KYC_InProgress",
  PASS = "PASS",
  FAIL = "FAIL",
  REFER = "REFER",
}

export interface KycStatusResponse {
  status: KycStatus | string;
  correlationId?: string;
}

export interface OnboardingState {
  currentStep: OnboardingStep;
  customerId: string | null;
  personalDetails: PersonalDetailsForm | null;
  documentId: string | null;
  documentsUploaded: boolean;
  kycStatus: KycStatus | null;
  applicationStatus: OnboardingStatus | null;
  correlationId: string | null;
}

export type OnboardingAction =
  | { type: "RESET" }
  | { type: "HYDRATE"; payload: OnboardingState }
  | { type: "SET_STEP"; payload: OnboardingStep }
  | { type: "RETRY_DOCUMENT_UPLOAD" }
  | {
      type: "SET_PERSONAL_DETAILS";
      payload: {
        customerId: string;
        details: PersonalDetailsForm;
        status: OnboardingStatus;
      };
    }
  | {
      type: "SET_DOCUMENT_UPLOADED";
      payload: {
        documentId: string;
      };
    }
  | {
      type: "SET_KYC_STATUS";
      payload: {
        status: KycStatus;
        correlationId?: string;
      };
    }
  | {
      type: "SET_APPLICATION_STATUS";
      payload: OnboardingStatus;
    };
