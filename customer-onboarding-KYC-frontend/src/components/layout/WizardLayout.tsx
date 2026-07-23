import { useEffect, useRef } from "react";
import { Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useOnboarding } from "../../hooks/useOnboarding";
import { ONBOARDING_STEP_LABELS } from "../../types/onboarding.types";

export const WizardLayout = () => {
  const headingRef = useRef<HTMLHeadingElement>(null);
  const { user } = useAuth();
  const {
    state: { currentStep },
  } = useOnboarding();

  const currentStepTitle = ONBOARDING_STEP_LABELS[currentStep];

  useEffect(() => {
    headingRef.current?.focus();
  }, [currentStep]);

  return (
    <section className="flex flex-1 flex-col gap-6 h-full min-h-full" data-e2e="onboarding-wizard">
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

      <div className="flex flex-1 flex-col rounded-xl border border-slate-200 bg-white p-4 sm:p-6 md:p-8 shadow-sm">
        <Outlet />
      </div>
    </section>
  );
};
