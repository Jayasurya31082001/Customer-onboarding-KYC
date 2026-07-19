import { useEffect } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useOnboarding } from "../hooks/useOnboarding";
import { clearActiveOnboardingEmail } from "../services/onboardingSessionService";
import { DocumentUploadPage } from "./DocumentUploadPage";
import { KycStatusPage } from "./KycStatusPage";
import { RegistrationPage } from "./RegistrationPage";
import { ReviewPage } from "./ReviewPage";
import { AccountConfirmationPage } from "./AccountConfirmationPage";
import { KycStatus, OnboardingStep } from "../types/onboarding.types";

export const OnboardingFlowPage = () => {
  const { isAuthenticated } = useAuth();
  const {
    state: { currentStep, customerId, documentsUploaded, kycStatus, personalDetails, documentId, applicationStatus, correlationId },
    dispatch,
  } = useOnboarding();

  useEffect(() => {
    if (isAuthenticated) {
      return;
    }

    clearActiveOnboardingEmail();

    if (
      currentStep !== OnboardingStep.PERSONAL_DETAILS ||
      customerId ||
      personalDetails ||
      documentId ||
      documentsUploaded ||
      kycStatus ||
      applicationStatus ||
      correlationId
    ) {
      dispatch({ type: "RESET" });
    }
  }, [
    applicationStatus,
    correlationId,
    currentStep,
    customerId,
    dispatch,
    documentId,
    documentsUploaded,
    isAuthenticated,
    kycStatus,
    personalDetails,
  ]);

  if (currentStep === OnboardingStep.PERSONAL_DETAILS) {
    return <RegistrationPage />;
  }

  if (currentStep === OnboardingStep.DOCUMENT_UPLOAD) {
    if (!customerId) {
      return <Navigate to="/onboarding" replace />;
    }
    return <DocumentUploadPage />;
  }

  if (currentStep === OnboardingStep.KYC_VERIFICATION) {
    if (!customerId || !documentsUploaded) {
      return <Navigate to="/onboarding" replace />;
    }
    return <KycStatusPage />;
  }

  if (currentStep === OnboardingStep.REVIEW) {
    if (kycStatus !== KycStatus.PASS) {
      return <KycStatusPage />;
    }
    return <ReviewPage />;
  }

  return <AccountConfirmationPage />;
};
