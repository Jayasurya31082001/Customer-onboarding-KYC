import { useState } from "react";
import type { ComplianceApplication } from "../../services/complianceService";

interface ComplianceAnalyticsChartsProps {
  applications: ComplianceApplication[];
}

export const ComplianceAnalyticsCharts = ({ applications }: ComplianceAnalyticsChartsProps) => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const total = applications.length;

  // Breakdown by Risk Score
  const highRiskCount = applications.filter((a) => a.riskScore !== null && a.riskScore > 70).length;
  const mediumRiskCount = applications.filter((a) => a.riskScore !== null && a.riskScore >= 40 && a.riskScore <= 70).length;
  const lowRiskCount = applications.filter((a) => a.riskScore === null || a.riskScore < 40).length;

  // Average Risk Score
  const scoredApps = applications.filter((a) => a.riskScore !== null);
  const avgRiskScore = scoredApps.length > 0
    ? Math.round(scoredApps.reduce((acc, curr) => acc + (curr.riskScore ?? 0), 0) / scoredApps.length)
    : "N/A";

  const getPercent = (count: number) => (total > 0 ? Math.round((count / total) * 100) : 0);

  return (
    <div data-e2e="compliance-analytics-charts">
      {/* Risk Profile Breakdown */}
      <div className="rounded-xl border border-slate-700/60 bg-slate-900/80 p-4 shadow-sm backdrop-blur">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-3">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400">
              Risk Score Distribution
            </h3>
            <span className="rounded bg-sky-500/20 px-2.5 py-0.5 text-xs font-medium text-sky-300">
              Avg Score: {avgRiskScore}
            </span>
          </div>

          <button
            type="button"
            onClick={() => setIsCollapsed((prev) => !prev)}
            className="rounded border border-slate-700 bg-slate-800 px-2.5 py-1 text-xs font-medium text-slate-300 transition-colors hover:bg-slate-700 hover:text-white"
            data-e2e="toggle-chart-collapse"
            aria-expanded={!isCollapsed}
          >
            {isCollapsed ? "Expand Chart ▲" : "Collapse Chart ▼"}
          </button>
        </div>

        <p className="mt-1.5 text-xl font-bold text-white">
          {total} <span className="text-xs font-normal text-slate-400">Total Referred Applications</span>
        </p>

        {!isCollapsed ? (
          <div className="mt-4 grid gap-4 md:grid-cols-3 transition-all duration-300">
            {/* High Risk */}
            <div className="rounded-lg bg-slate-800/60 p-3">
              <div className="flex justify-between text-xs font-medium text-slate-300">
                <span className="flex items-center gap-1.5">
                  <span className="h-2 w-2 rounded-full bg-rose-500" />
                  High Risk (&gt;70)
                </span>
                <span>{highRiskCount} ({getPercent(highRiskCount)}%)</span>
              </div>
              <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-800">
                <div
                  className="h-full bg-rose-500 transition-all duration-500"
                  style={{ width: `${getPercent(highRiskCount)}%` }}
                />
              </div>
            </div>

            {/* Medium Risk */}
            <div className="rounded-lg bg-slate-800/60 p-3">
              <div className="flex justify-between text-xs font-medium text-slate-300">
                <span className="flex items-center gap-1.5">
                  <span className="h-2 w-2 rounded-full bg-amber-500" />
                  Medium Risk (40-70)
                </span>
                <span>{mediumRiskCount} ({getPercent(mediumRiskCount)}%)</span>
              </div>
              <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-800">
                <div
                  className="h-full bg-amber-500 transition-all duration-500"
                  style={{ width: `${getPercent(mediumRiskCount)}%` }}
                />
              </div>
            </div>

            {/* Low Risk */}
            <div className="rounded-lg bg-slate-800/60 p-3">
              <div className="flex justify-between text-xs font-medium text-slate-300">
                <span className="flex items-center gap-1.5">
                  <span className="h-2 w-2 rounded-full bg-emerald-500" />
                  Low / Unassessed (&lt;40)
                </span>
                <span>{lowRiskCount} ({getPercent(lowRiskCount)}%)</span>
              </div>
              <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-800">
                <div
                  className="h-full bg-emerald-500 transition-all duration-500"
                  style={{ width: `${getPercent(lowRiskCount)}%` }}
                />
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};
