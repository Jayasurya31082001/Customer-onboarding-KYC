import type { ComplianceApplication } from "../../services/complianceService";
import { documentService } from "../../services/documentService";
import { RiskScoreChart } from "./RiskScoreChart";

interface ApplicationDetailPanelProps {
  application: ComplianceApplication | null;
  onOpenDecisionModal: () => void;
}

export const ApplicationDetailPanel = ({
  application,
  onOpenDecisionModal,
}: ApplicationDetailPanelProps) => {
  const handleDownloadDocument = async (): Promise<void> => {
    if (!application) {
      return;
    }

    const { blob, fileName } = await documentService.downloadLatestDocumentByCustomer(application.customerId);
    const downloadUrl = window.URL.createObjectURL(blob);
    const anchor = window.document.createElement("a");
    anchor.href = downloadUrl;
    anchor.download = fileName;
    window.document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.URL.revokeObjectURL(downloadUrl);
  };

  if (!application) {
    return (
      <div className="rounded-lg border border-dashed border-slate-300 bg-white p-6 text-sm text-slate-800">
        Select an application to view details.
      </div>
    );
  }

  return (
    <section className="space-y-4 rounded-lg border border-slate-200 bg-white p-4 text-slate-900" data-e2e="application-detail-panel">
      <h2 className="text-lg font-semibold text-slate-900">Application Details</h2>
      <dl className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-slate-700">Customer ID</dt>
          <dd className="font-mono text-slate-900">{application.customerId}</dd>
        </div>
        <div>
          <dt className="text-slate-700">Email</dt>
          <dd className="text-slate-900">{application.email}</dd>
        </div>
        <div>
          <dt className="text-slate-700">Assessed At</dt>
          <dd className="text-slate-900">{new Date(application.assessedAt).toLocaleString()}</dd>
        </div>
        <div>
          <dt className="text-slate-700">Suggested Disposition</dt>
          <dd className="text-slate-900">{application.disposition}</dd>
        </div>
      </dl>

      <RiskScoreChart score={application.riskScore} />

      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => {
            void handleDownloadDocument();
          }}
          className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
          data-e2e="download-document-button"
        >
          Download Document
        </button>

        <button
          type="button"
          onClick={onOpenDecisionModal}
          className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700"
          data-e2e="open-decision-modal"
        >
          Record Decision
        </button>
      </div>
    </section>
  );
};
