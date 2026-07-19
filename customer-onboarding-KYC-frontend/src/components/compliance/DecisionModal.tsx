import { useState } from "react";
import type { ComplianceDecision } from "../../services/complianceService";

interface DecisionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (decision: ComplianceDecision, reason: string) => Promise<void>;
}

const decisions: ComplianceDecision[] = ["APPROVE", "REJECT", "REFER"];

export const DecisionModal = ({ isOpen, onClose, onSubmit }: DecisionModalProps) => {
  const [decision, setDecision] = useState<ComplianceDecision>("APPROVE");
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) {
    return null;
  }

  const handleSubmit = async () => {
    if (!reason.trim()) {
      setError("Reason is required.");
      return;
    }

    setError(null);
    setIsSubmitting(true);

    try {
      await onSubmit(decision, reason.trim());
      onClose();
      setReason("");
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
        <p className="mt-1 text-sm text-slate-700">Choose a decision and record an audit reason.</p>

        <label htmlFor="decision" className="mt-4 block text-sm font-medium text-slate-900">
          Decision
        </label>
        <select
          id="decision"
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
          value={decision}
          onChange={(event) => setDecision(event.target.value as ComplianceDecision)}
          data-e2e="decision-select"
        >
          {decisions.map((option) => (
            <option value={option} key={option}>
              {option}
            </option>
          ))}
        </select>

        <label htmlFor="decisionReason" className="mt-3 block text-sm font-medium text-slate-900">
          Reason
        </label>
        <textarea
          id="decisionReason"
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
          rows={3}
          value={reason}
          onChange={(event) => setReason(event.target.value)}
          data-e2e="decision-reason"
        />

        {error ? (
          <p role="alert" className="mt-2 text-sm text-red-700">
            {error}
          </p>
        ) : null}

        <div className="mt-4 flex justify-end gap-2">
          <button type="button" onClick={onClose} className="rounded border border-slate-300 px-3 py-2 text-sm">
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            className="rounded bg-slate-900 px-3 py-2 text-sm text-white hover:bg-slate-700"
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
