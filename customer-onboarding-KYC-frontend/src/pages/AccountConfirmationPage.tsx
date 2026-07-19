import { useQuery } from "@tanstack/react-query";
import { accountService } from "../services/accountService";
import { useOnboarding } from "../hooks/useOnboarding";

export const AccountConfirmationPage = () => {
  const {
    state: { customerId },
  } = useOnboarding();

  const accountQuery = useQuery({
    queryKey: ["account", customerId],
    queryFn: async () => {
      if (!customerId) {
        throw new Error("Customer ID is unavailable.");
      }
      return accountService.getAccountByCustomer(customerId);
    },
    enabled: Boolean(customerId),
  });

  return (
    <section aria-labelledby="confirmation-heading" data-e2e="confirmation-page" className="space-y-4">
      <h2 id="confirmation-heading" className="text-2xl font-bold text-slate-900">
        Step 5: Confirmation
      </h2>
      <p className="text-sm text-slate-700">Your onboarding has been submitted. Review account details below.</p>

      {accountQuery.isLoading ? (
        <p role="status" className="rounded-lg border border-sky-200 bg-sky-50 p-3 text-sm font-medium text-sky-900">
          Loading account confirmation...
        </p>
      ) : null}

      {accountQuery.error ? (
        <p className="rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm font-medium text-amber-900" role="status">
          Account is not yet available. Approval may still be in progress.
        </p>
      ) : null}

      {accountQuery.data ? (
        <div className="rounded-xl border border-emerald-300 bg-gradient-to-br from-emerald-100 to-lime-50 p-5 text-base text-emerald-950 shadow-sm">
          <p className="text-lg font-bold">Application approved and account created.</p>
          <p className="mt-3">Account number: <span className="font-semibold">{accountQuery.data.accountNumber}</span></p>
          <p>Sort code: <span className="font-semibold">{accountQuery.data.sortCode}</span></p>
          <p>Status: <span className="font-semibold">{accountQuery.data.status}</span></p>
        </div>
      ) : null}
    </section>
  );
};
