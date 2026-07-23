import { useState } from "react";

export type FilterOperator = "CONTAINS" | "EQUALS" | "STARTS_WITH";
export type RiskOperator = "ALL" | "GREATER_THAN" | "LESS_THAN" | "EQUALS";

export interface ComplianceFilterState {
  customerIdOp: FilterOperator;
  customerId: string;
  customerNameOp: FilterOperator;
  customerName: string;
  emailOp: FilterOperator;
  email: string;
  riskScoreOp: RiskOperator;
  riskScore: string;
}

interface ComplianceFiltersProps {
  filters: ComplianceFilterState;
  onFilterChange: (updated: Partial<ComplianceFilterState>) => void;
  onResetFilters: () => void;
  totalMatches: number;
  totalRecords: number;
}

export const ComplianceFilters = ({
  filters,
  onFilterChange,
  onResetFilters,
  totalMatches,
  totalRecords,
}: ComplianceFiltersProps) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  const isFiltered =
    filters.customerId.trim() !== "" ||
    filters.customerName.trim() !== "" ||
    filters.email.trim() !== "" ||
    (filters.riskScoreOp !== "ALL" && filters.riskScore.trim() !== "");

  return (
    <div className="rounded-xl border border-slate-700/60 bg-slate-900/80 p-4 shadow-sm backdrop-blur" data-e2e="compliance-filters">
      <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-800">
        <h3 className="text-sm font-semibold text-white flex items-center gap-2">
          <span>Advanced Field Filters</span>
          <span className="rounded-full bg-sky-500/20 px-2 py-0.5 text-xs text-sky-300 font-normal">
            Showing {totalMatches} of {totalRecords}
          </span>
        </h3>

        <div className="flex items-center gap-3">
          {isFiltered ? (
            <button
              type="button"
              onClick={onResetFilters}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium underline underline-offset-2"
              data-e2e="reset-filters-btn"
            >
              Clear all filters
            </button>
          ) : null}

          <button
            type="button"
            onClick={() => setIsCollapsed((prev) => !prev)}
            className="rounded border border-slate-700 bg-slate-800 px-2.5 py-1 text-xs font-medium text-slate-300 transition-colors hover:bg-slate-700 hover:text-white"
            data-e2e="toggle-filters-collapse"
            aria-expanded={!isCollapsed}
          >
            {isCollapsed ? "Expand Filters ▲" : "Collapse Filters ▼"}
          </button>
        </div>
      </div>

      {!isCollapsed ? (
        <div className="mt-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4 transition-all duration-300">
          {/* Customer ID */}
          <div>
            <label htmlFor="filter-customer-id" className="block text-xs font-medium text-slate-300">
              Customer ID
            </label>
            <div className="mt-1 flex rounded-lg shadow-sm">
              <select
                value={filters.customerIdOp}
                onChange={(e) => onFilterChange({ customerIdOp: e.target.value as FilterOperator })}
                className="rounded-l-lg border border-r-0 border-slate-700 bg-slate-800/90 px-2 py-1.5 text-xs text-slate-300 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-customer-id-op"
              >
                <option value="CONTAINS">Contains</option>
                <option value="EQUALS">Equals</option>
                <option value="STARTS_WITH">Starts with</option>
              </select>
              <input
                id="filter-customer-id"
                type="text"
                placeholder="Customer ID..."
                value={filters.customerId}
                onChange={(e) => onFilterChange({ customerId: e.target.value })}
                className="w-full rounded-r-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs text-white placeholder-slate-400 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-customer-id-input"
              />
            </div>
          </div>

          {/* Customer Name */}
          <div>
            <label htmlFor="filter-customer-name" className="block text-xs font-medium text-slate-300">
              Customer Name
            </label>
            <div className="mt-1 flex rounded-lg shadow-sm">
              <select
                value={filters.customerNameOp}
                onChange={(e) => onFilterChange({ customerNameOp: e.target.value as FilterOperator })}
                className="rounded-l-lg border border-r-0 border-slate-700 bg-slate-800/90 px-2 py-1.5 text-xs text-slate-300 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-customer-name-op"
              >
                <option value="CONTAINS">Contains</option>
                <option value="EQUALS">Equals</option>
                <option value="STARTS_WITH">Starts with</option>
              </select>
              <input
                id="filter-customer-name"
                type="text"
                placeholder="Customer Name..."
                value={filters.customerName}
                onChange={(e) => onFilterChange({ customerName: e.target.value })}
                className="w-full rounded-r-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs text-white placeholder-slate-400 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-customer-name-input"
              />
            </div>
          </div>

          {/* Email Address */}
          <div>
            <label htmlFor="filter-email" className="block text-xs font-medium text-slate-300">
              Email Address
            </label>
            <div className="mt-1 flex rounded-lg shadow-sm">
              <select
                value={filters.emailOp}
                onChange={(e) => onFilterChange({ emailOp: e.target.value as FilterOperator })}
                className="rounded-l-lg border border-r-0 border-slate-700 bg-slate-800/90 px-2 py-1.5 text-xs text-slate-300 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-email-op"
              >
                <option value="CONTAINS">Contains</option>
                <option value="EQUALS">Equals</option>
                <option value="STARTS_WITH">Starts with</option>
              </select>
              <input
                id="filter-email"
                type="text"
                placeholder="Email..."
                value={filters.email}
                onChange={(e) => onFilterChange({ email: e.target.value })}
                className="w-full rounded-r-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs text-white placeholder-slate-400 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-email-input"
              />
            </div>
          </div>

          {/* Risk Score */}
          <div>
            <label htmlFor="filter-risk-score" className="block text-xs font-medium text-slate-300">
              Risk Score
            </label>
            <div className="mt-1 flex rounded-lg shadow-sm">
              <select
                value={filters.riskScoreOp}
                onChange={(e) => onFilterChange({ riskScoreOp: e.target.value as RiskOperator })}
                className="rounded-l-lg border border-r-0 border-slate-700 bg-slate-800/90 px-2 py-1.5 text-xs text-slate-300 focus:border-sky-500 focus:outline-none"
                data-e2e="filter-risk-score-op"
              >
                <option value="ALL">All</option>
                <option value="GREATER_THAN">&gt; (Greater than)</option>
                <option value="LESS_THAN">&lt; (Less than)</option>
                <option value="EQUALS">= (Equals)</option>
              </select>
              <input
                id="filter-risk-score"
                type="number"
                placeholder={filters.riskScoreOp === "ALL" ? "Score..." : "Value (0-100)..."}
                disabled={filters.riskScoreOp === "ALL"}
                value={filters.riskScore}
                onChange={(e) => onFilterChange({ riskScore: e.target.value })}
                className="w-full rounded-r-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs text-white placeholder-slate-400 focus:border-sky-500 focus:outline-none disabled:opacity-50"
                data-e2e="filter-risk-score-input"
              />
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
};
