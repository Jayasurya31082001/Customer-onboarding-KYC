import { useMemo, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { ApplicationDetailPanel } from "../components/compliance/ApplicationDetailPanel";
import { ApplicationsTable } from "../components/compliance/ApplicationsTable";
import { DecisionModal } from "../components/compliance/DecisionModal";
import { ComplianceAnalyticsCharts } from "../components/compliance/ComplianceAnalyticsCharts";
import {
  ComplianceFilters,
  type ComplianceFilterState,
  type FilterOperator,
  type RiskOperator,
} from "../components/compliance/ComplianceFilters";
import {
  complianceService,
  type ComplianceApplication,
  type ComplianceDecision,
} from "../services/complianceService";

const initialFilterState: ComplianceFilterState = {
  customerIdOp: "CONTAINS",
  customerId: "",
  customerNameOp: "CONTAINS",
  customerName: "",
  emailOp: "CONTAINS",
  email: "",
  riskScoreOp: "ALL",
  riskScore: "",
};

const matchText = (value: string, query: string, op: FilterOperator): boolean => {
  if (!query.trim()) return true;
  const val = value.toLowerCase();
  const q = query.trim().toLowerCase();
  if (op === "EQUALS") return val === q;
  if (op === "STARTS_WITH") return val.startsWith(q);
  return val.includes(q); // CONTAINS
};

const matchScore = (score: number | null, query: string, op: RiskOperator): boolean => {
  if (op === "ALL" || !query.trim()) return true;
  const target = Number(query.trim());
  if (Number.isNaN(target)) return true;
  if (score === null) return false;
  if (op === "GREATER_THAN") return score > target;
  if (op === "LESS_THAN") return score < target;
  if (op === "EQUALS") return score === target;
  return true;
};

export const ComplianceDashboard = () => {
  const queryClient = useQueryClient();
  const [selectedApplication, setSelectedApplication] = useState<ComplianceApplication | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filters, setFilters] = useState<ComplianceFilterState>(initialFilterState);

  const applicationsQuery = useQuery({
    queryKey: ["compliance-applications"],
    queryFn: complianceService.getApplications,
    staleTime: 0,
    refetchOnMount: "always",
    refetchInterval: 5_000,
  });

  const allApplications = useMemo(() => applicationsQuery.data ?? [], [applicationsQuery.data]);

  const filteredApplications = useMemo(() => {
    return allApplications.filter((app) => {
      // Customer ID
      if (!matchText(app.customerId, filters.customerId, filters.customerIdOp)) {
        return false;
      }

      // Customer Name
      const fullName = `${app.firstName ?? ""} ${app.lastName ?? ""}`.trim();
      if (!matchText(fullName, filters.customerName, filters.customerNameOp)) {
        return false;
      }

      // Email
      if (!matchText(app.email, filters.email, filters.emailOp)) {
        return false;
      }

      // Risk Score
      if (!matchScore(app.riskScore, filters.riskScore, filters.riskScoreOp)) {
        return false;
      }

      return true;
    });
  }, [allApplications, filters]);

  const selectedCustomerId = useMemo(() => selectedApplication?.customerId ?? null, [selectedApplication]);
  const recordCount = allApplications.length;
  const lastFetchedAt = applicationsQuery.dataUpdatedAt
    ? new Date(applicationsQuery.dataUpdatedAt).toLocaleString()
    : "Not fetched yet";

  const handleFilterChange = (updated: Partial<ComplianceFilterState>) => {
    setFilters((prev) => ({ ...prev, ...updated }));
  };

  const handleResetFilters = () => {
    setFilters(initialFilterState);
  };

  const handleSubmitDecision = async (decision: ComplianceDecision) => {
    if (!selectedApplication) {
      throw new Error("No application selected.");
    }

    const decidedCustomerId = selectedApplication.customerId;

    await complianceService.submitDecision({
      customerId: decidedCustomerId,
      customerEmail: selectedApplication.email,
      decision,
    });

    if (decision === "APPROVE" || decision === "REJECT") {
      queryClient.setQueryData<ComplianceApplication[]>(["compliance-applications"], (existing) =>
        (existing ?? []).filter((application) => application.customerId !== decidedCustomerId),
      );
      setSelectedApplication(null);
    }

    await applicationsQuery.refetch();
  };

  return (
    <section className="space-y-5 text-slate-100" data-e2e="compliance-dashboard-page">
      <header className="rounded-xl border border-white/20 bg-slate-900/50 px-4 py-3 shadow-sm backdrop-blur">
        <h1 className="text-2xl font-extrabold tracking-tight text-white">Compliance Agent Dashboard</h1>
        <p className="mt-1 text-sm font-medium text-slate-100">Review referred applications and record final decisions.</p>
        <div className="mt-3 grid gap-2 text-xs text-slate-100 sm:grid-cols-2">
          <p className="rounded border border-white/20 bg-white/10 px-2.5 py-1.5" data-e2e="compliance-last-fetch">
            Last fetch: {lastFetchedAt}
          </p>
          <p className="rounded border border-white/20 bg-white/10 px-2.5 py-1.5" data-e2e="compliance-record-count">
            Records returned: {recordCount}
          </p>
        </div>
        <div className="mt-3">
          <button
            type="button"
            onClick={() => {
              void applicationsQuery.refetch();
            }}
            className="rounded border border-white/30 bg-white/10 px-3 py-1.5 text-xs font-semibold text-white hover:bg-white/20"
            data-e2e="compliance-refresh"
          >
            Refresh Records
          </button>
        </div>
      </header>

      {/* Analytics Summary & Charts */}
      <ComplianceAnalyticsCharts applications={allApplications} />

      {/* Filters Bar */}
      <ComplianceFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onResetFilters={handleResetFilters}
        totalMatches={filteredApplications.length}
        totalRecords={recordCount}
      />

      {applicationsQuery.isLoading ? <p role="status">Loading applications...</p> : null}

      {applicationsQuery.error ? (
        <p role="alert" className="rounded bg-red-50 p-3 text-sm text-red-700">
          {applicationsQuery.error instanceof Error
            ? applicationsQuery.error.message
            : "Unable to load applications."}
        </p>
      ) : null}

      {/* Table & Detail Container */}
      <div className="flex flex-col gap-4">
        <ApplicationsTable
          applications={filteredApplications}
          selectedCustomerId={selectedCustomerId}
          onSelect={setSelectedApplication}
        />
        <ApplicationDetailPanel application={selectedApplication} onOpenDecisionModal={() => setIsModalOpen(true)} />
      </div>

      <DecisionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitDecision}
      />
    </section>
  );
};
