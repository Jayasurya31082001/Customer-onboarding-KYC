import { useQuery } from "@tanstack/react-query";
import type { ComplianceApplication } from "../../services/complianceService";
import { customerService } from "../../services/customerService";
import { documentService } from "../../services/documentService";
import { getOnboardingSessionByCustomerId } from "../../services/onboardingSessionService";
import { RiskScoreChart } from "./RiskScoreChart";

interface ApplicationDetailPanelProps {
  application: ComplianceApplication | null;
  onOpenDecisionModal: () => void;
}

export const ApplicationDetailPanel = ({
  application,
  onOpenDecisionModal,
}: ApplicationDetailPanelProps) => {
  const customerId = application?.customerId ?? null;

  // Live lookup from customerService + local session fallback
  const customerQuery = useQuery({
    queryKey: ["customer-detail", customerId],
    queryFn: async () => {
      if (!customerId) return null;
      try {
        return await customerService.getCustomer(customerId);
      } catch {
        return null;
      }
    },
    enabled: Boolean(customerId),
  });

  const session = customerId ? getOnboardingSessionByCustomerId(customerId) : null;
  const liveCustomer = customerQuery.data;

  const firstName = liveCustomer?.firstName ?? application?.firstName ?? session?.personalDetails?.firstName ?? "N/A";
  const lastName = liveCustomer?.lastName ?? application?.lastName ?? session?.personalDetails?.lastName ?? "N/A";
  const fullName = firstName !== "N/A" || lastName !== "N/A" ? `${firstName} ${lastName}`.trim() : "N/A";
  const email = liveCustomer?.email ?? application?.email ?? session?.personalDetails?.email ?? "N/A";
  const dateOfBirth = liveCustomer?.dateOfBirth ?? application?.dateOfBirth ?? session?.personalDetails?.dateOfBirth ?? "N/A";
  const phoneNumber = liveCustomer?.phoneNumber ?? application?.phoneNumber ?? session?.personalDetails?.phoneNumber ?? "N/A";
  const nationality = liveCustomer?.nationality ?? application?.nationality ?? session?.personalDetails?.nationality ?? "N/A";
  const addressLine1 = liveCustomer?.addressLine1 ?? application?.addressLine1 ?? session?.personalDetails?.addressLine1 ?? "N/A";
  const city = liveCustomer?.city ?? application?.city ?? session?.personalDetails?.city ?? "N/A";
  const postcode = liveCustomer?.postcode ?? application?.postcode ?? session?.personalDetails?.postcode ?? "N/A";

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
    <section className="space-y-5 rounded-lg border border-slate-200 bg-white p-5 text-slate-900 shadow-sm" data-e2e="application-detail-panel">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
        <div>
          <h2 className="text-lg font-bold text-slate-900">Application Details</h2>
          <p className="text-xs text-slate-500 font-mono">ID: {application.customerId}</p>
        </div>
        <span className="rounded bg-sky-100 px-2.5 py-1 text-xs font-semibold text-sky-800">
          Status: {application.status}
        </span>
      </div>

      {/* Customer Personal & Contact Info */}
      <div className="rounded-lg border border-slate-100 bg-slate-50/70 p-4">
        <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-700">
          Personal Information
        </h3>
        <dl className="mt-3 grid grid-cols-1 gap-3 text-sm sm:grid-cols-2 lg:grid-cols-3">
          <div>
            <dt className="text-xs text-slate-700">Full Name</dt>
            <dd className="font-semibold text-slate-900">{fullName}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">Email Address</dt>
            <dd className="font-medium text-slate-900">{email}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">Phone Number</dt>
            <dd className="font-medium text-slate-900">{phoneNumber}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">Date of Birth</dt>
            <dd className="font-medium text-slate-900">{dateOfBirth}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">Nationality</dt>
            <dd className="font-medium text-slate-900">{nationality}</dd>
          </div>
        </dl>
      </div>

      {/* Customer Address Info */}
      <div className="rounded-lg border border-slate-100 bg-slate-50/70 p-4">
        <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-700">
          Address Information
        </h3>
        <dl className="mt-3 grid grid-cols-1 gap-3 text-sm sm:grid-cols-3">
          <div>
            <dt className="text-xs text-slate-700">Address Line 1</dt>
            <dd className="font-medium text-slate-900">{addressLine1}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">City</dt>
            <dd className="font-medium text-slate-900">{city}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">UK Postcode</dt>
            <dd className="font-medium text-slate-900">{postcode}</dd>
          </div>
        </dl>
      </div>

      {/* Risk & Assessment Details */}
      <div className="rounded-lg border border-slate-100 bg-slate-50/70 p-4">
        <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-700">
          Compliance & Risk Assessment
        </h3>
        <dl className="mt-3 grid grid-cols-1 gap-3 text-sm sm:grid-cols-2 mb-3">
          <div>
            <dt className="text-xs text-slate-700">Assessed At</dt>
            <dd className="font-medium text-slate-900">{new Date(application.assessedAt).toLocaleString()}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-700">Suggested Disposition</dt>
            <dd className="font-semibold text-slate-900">{application.disposition}</dd>
          </div>
        </dl>

        <RiskScoreChart score={application.riskScore} />
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-2 pt-2">
        <button
          type="button"
          onClick={() => {
            void handleDownloadDocument();
          }}
          className="rounded border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-50 shadow-sm disabled:cursor-not-allowed disabled:opacity-50"
          data-e2e="download-document-button"
        >
          Download Document
        </button>

        <button
          type="button"
          onClick={onOpenDecisionModal}
          className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 shadow-sm"
          data-e2e="open-decision-modal"
        >
          Record Decision
        </button>
      </div>
    </section>
  );
};
