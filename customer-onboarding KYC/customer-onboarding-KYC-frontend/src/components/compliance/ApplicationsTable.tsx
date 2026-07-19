import type { ComplianceApplication } from "../../services/complianceService";

interface ApplicationsTableProps {
  applications: ComplianceApplication[];
  selectedCustomerId: string | null;
  onSelect: (application: ComplianceApplication) => void;
}

export const ApplicationsTable = ({
  applications,
  selectedCustomerId,
  onSelect,
}: ApplicationsTableProps) => {
  if (applications.length === 0) {
    return (
      <p className="rounded border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900" role="status">
        No customers are currently in MANUAL_APPROVAL_REQUIRED status.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-900" data-e2e="applications-table">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-3 py-2 font-semibold text-slate-900">Customer ID</th>
            <th className="px-3 py-2 font-semibold text-slate-900">Email</th>
            <th className="px-3 py-2 font-semibold text-slate-900">Risk Score</th>
            <th className="px-3 py-2 font-semibold text-slate-900">Disposition</th>
            <th className="px-3 py-2 font-semibold text-slate-900">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {applications.map((application) => {
            const isSelected = application.customerId === selectedCustomerId;
            return (
              <tr
                key={application.customerId}
                className={isSelected ? "bg-sky-50" : "hover:bg-slate-50"}
                onClick={() => onSelect(application)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onSelect(application);
                  }
                }}
                tabIndex={0}
                role="button"
                aria-pressed={isSelected}
                data-e2e={`application-row-${application.customerId}`}
              >
                <td className="px-3 py-2 font-mono text-xs text-slate-900 sm:text-sm">{application.customerId}</td>
                <td className="px-3 py-2 text-slate-900">{application.email}</td>
                <td className="px-3 py-2 text-slate-900">{application.riskScore ?? "N/A"}</td>
                <td className="px-3 py-2 text-slate-900">{application.disposition}</td>
                <td className="px-3 py-2 text-slate-900">{application.status}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
