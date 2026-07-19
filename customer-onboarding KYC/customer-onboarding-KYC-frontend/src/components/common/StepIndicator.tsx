import { OnboardingStep } from "../../types/onboarding.types";

const stepLabels: Array<{ step: OnboardingStep; label: string }> = [
  { step: OnboardingStep.PERSONAL_DETAILS, label: "Personal Details" },
  { step: OnboardingStep.DOCUMENT_UPLOAD, label: "Document Upload" },
  { step: OnboardingStep.KYC_VERIFICATION, label: "KYC Verification" },
  { step: OnboardingStep.REVIEW, label: "Review" },
  { step: OnboardingStep.CONFIRMATION, label: "Confirmation" },
];

interface StepIndicatorProps {
  currentStep: OnboardingStep;
}

export const StepIndicator = ({ currentStep }: StepIndicatorProps) => {
  const currentIndex = stepLabels.findIndex((entry) => entry.step === currentStep);

  return (
    <ol className="grid grid-cols-1 gap-2 sm:grid-cols-2 xl:grid-cols-5" aria-label="Onboarding progress">
      {stepLabels.map((entry, index) => {
        const isActive = entry.step === currentStep;
        const isCompleted = index < currentIndex;

        return (
          <li
            key={entry.step}
            className={[
              "rounded-lg border px-3 py-2 text-sm",
              isActive ? "border-sky-500 bg-sky-50 text-sky-900" : "border-slate-300 bg-white text-slate-700",
              isCompleted ? "ring-1 ring-emerald-300" : "",
            ].join(" ")}
            aria-current={isActive ? "step" : undefined}
            data-e2e={`step-${entry.step.toLowerCase()}`}
          >
            <span className="font-semibold">{index + 1}. </span>
            {entry.label}
          </li>
        );
      })}
    </ol>
  );
};
