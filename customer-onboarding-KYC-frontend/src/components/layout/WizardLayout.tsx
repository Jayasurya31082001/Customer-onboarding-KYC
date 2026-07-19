import { useEffect, useRef } from "react";
import { Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useOnboarding } from "../../hooks/useOnboarding";
import { OnboardingStep } from "../../types/onboarding.types";

const stepTitleByStep: Record<OnboardingStep, string> = {
  [OnboardingStep.PERSONAL_DETAILS]: "Personal Details",
  [OnboardingStep.DOCUMENT_UPLOAD]: "Document Upload",
  [OnboardingStep.KYC_VERIFICATION]: "KYC Verification",
  [OnboardingStep.REVIEW]: "Review",
  [OnboardingStep.CONFIRMATION]: "Confirmation",
};

export const WizardLayout = () => {
  const headingRef = useRef<HTMLHeadingElement>(null);
  const { user } = useAuth();
  const {
    state: { currentStep },
  } = useOnboarding();

  const currentStepTitle = stepTitleByStep[currentStep];

  useEffect(() => {
    headingRef.current?.focus();
  }, [currentStep]);

  return (
    <section className="flex h-full min-h-full flex-col gap-6" data-e2e="onboarding-wizard">
      <header className="space-y-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
        <h1 ref={headingRef} tabIndex={-1} className="text-2xl font-bold text-slate-900 focus:outline-none">
          Multi-step Customer Onboarding
        </h1>
        <p className="text-sm text-slate-700">
          Complete your profile, upload documents, and track KYC results in real-time.
        </p>
        {user ? (
          <p className="inline-flex w-fit rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700" data-e2e="signed-in-banner">
            Signed in as {user.email}
          </p>
        ) : null}
        <p className="inline-flex w-fit rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-800" aria-live="polite" data-e2e="current-step-banner">
          {currentStepTitle}
        </p>
      </header>

      <div className="flex-1 rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
        <Outlet />
      </div>
    </section>
  );
};
