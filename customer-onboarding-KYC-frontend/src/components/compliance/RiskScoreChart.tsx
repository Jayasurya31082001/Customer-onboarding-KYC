interface RiskScoreChartProps {
  score: number | null;
}

export const RiskScoreChart = ({ score }: RiskScoreChartProps) => {
  if (score === null) {
    return (
      <section aria-label="Risk score" className="space-y-2" data-e2e="risk-score-chart">
        <div className="flex items-center justify-between text-sm">
          <span className="font-medium">Risk Score</span>
          <span>N/A</span>
        </div>
        <p className="text-xs text-slate-600">Latest score is unavailable from Risk service.</p>
      </section>
    );
  }

  const boundedScore = Math.max(0, Math.min(100, score));

  const colorClass =
    boundedScore >= 75 ? "bg-red-500" : boundedScore >= 50 ? "bg-amber-500" : "bg-emerald-500";

  return (
    <section aria-label="Risk score" className="space-y-2" data-e2e="risk-score-chart">
      <div className="flex items-center justify-between text-sm">
        <span className="font-medium">Risk Score</span>
        <span>{boundedScore}/100</span>
      </div>
      <div className="h-3 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full ${colorClass}`}
          style={{ width: `${boundedScore}%` }}
          aria-hidden="true"
        />
      </div>
    </section>
  );
};
