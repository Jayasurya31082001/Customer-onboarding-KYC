import { useNavigate } from "react-router-dom";
import { useOnboarding } from "../hooks/useOnboarding";
import { customerService } from "../services/customerService";
import { OnboardingStatus } from "../types/customer.types";

export const ReviewPage = () => {
  const navigate = useNavigate();
  const {
    state: { customerId, personalDetails },
    dispatch,
  } = useOnboarding();

  const handleConfirm = async () => {
    if (!customerId) {
      return;
    }

    const response = await customerService.updateCustomerStatus(customerId, OnboardingStatus.KYC_COMPLETED);
    dispatch({
      type: "SET_APPLICATION_STATUS",
      payload: response.status,
    });
    navigate("/onboarding/confirmation", { replace: true });
  };

  return (
    <section aria-labelledby="review-heading" data-e2e="review-page">
      <h2 id="review-heading" className="text-2xl font-bold text-slate-900">
        Step 4: Review
      </h2>
      <p className="mt-2 text-sm text-slate-700">Confirm the details below before final submission.</p>

      <dl className="mt-4 grid grid-cols-1 gap-3 rounded border border-slate-300 bg-slate-50 p-4 sm:grid-cols-2">
        <div>
          <dt className="text-xs font-semibold uppercase tracking-wide text-slate-700">Name</dt>
          <dd className="mt-1 text-base font-semibold text-slate-900">{personalDetails ? `${personalDetails.firstName} ${personalDetails.lastName}` : "-"}</dd>
        </div>
        <div>
          <dt className="text-xs font-semibold uppercase tracking-wide text-slate-700">Email</dt>
          <dd className="mt-1 text-base font-medium text-slate-900">{personalDetails?.email ?? "-"}</dd>
        </div>
        <div>
          <dt className="text-xs font-semibold uppercase tracking-wide text-slate-700">Phone</dt>
          <dd className="mt-1 text-base font-medium text-slate-900">{personalDetails?.phoneNumber ?? "-"}</dd>
        </div>
        <div>
          <dt className="text-xs font-semibold uppercase tracking-wide text-slate-700">Postcode</dt>
          <dd className="mt-1 text-base font-medium text-slate-900">{personalDetails?.postcode ?? "-"}</dd>
        </div>
      </dl>

      <button
        type="button"
        className="mt-4 rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700"
        onClick={() => {
          void handleConfirm();
        }}
      >
        Confirm Application
      </button>
    </section>
  );
};
