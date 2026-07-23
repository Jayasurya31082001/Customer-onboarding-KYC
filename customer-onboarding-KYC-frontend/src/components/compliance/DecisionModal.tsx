import { useState } from "react";
import type { ComplianceDecision } from "../../services/complianceService";

interface DecisionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (decision: ComplianceDecision) => Promise<void>;
}

const decisionOptions: { value: ComplianceDecision; label: string }[] = [
  { value: "APPROVE", label: "Accept" },
  { value: "REJECT", label: "Reject" },
];

export const DecisionModal = ({ isOpen, onClose, onSubmit }: DecisionModalProps) => {
  const [decision, setDecision] = useState<ComplianceDecision>("APPROVE");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) {
    return null;
  }

  const handleSubmit = async () => {
    setError(null);
    setIsSubmitting(true);

    try {
      await onSubmit(decision);
      onClose();
      setDecision("APPROVE");
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to submit decision.";
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-slate-900/60 p-4" role="dialog" aria-modal="true">
      <div className="w-full max-w-md rounded-lg bg-white p-5 text-slate-900 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-900">Decision</h3>
        <p className="mt-1 text-sm text-slate-700">Choose a decision for this application.</p>

        <label htmlFor="decision" className="mt-4 block text-sm font-medium text-slate-900">
          Decision
        </label>
        <select
          id="decision"
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm focus:border-slate-900 focus:outline-none"
          value={decision}
          onChange={(event) => setDecision(event.target.value as ComplianceDecision)}
          data-e2e="decision-select"
        >
          {decisionOptions.map((option) => (
            <option value={option.value} key={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        {error ? (
          <p role="alert" className="mt-2 text-sm text-red-700">
            {error}
          </p>
        ) : null}

        <div className="mt-4 flex justify-end gap-2">
          <button type="button" onClick={onClose} className="rounded border border-slate-300 px-3 py-2 text-sm font-medium hover:bg-slate-50">
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
            disabled={isSubmitting}
            data-e2e="decision-submit"
          >
            {isSubmitting ? "Submitting..." : "Submit"}
          </button>
        </div>
      </div>
    </div>
  );
};
