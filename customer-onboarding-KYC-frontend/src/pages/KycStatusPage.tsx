import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useOnboarding } from "../hooks/useOnboarding";
import { customerService } from "../services/customerService";
import { kycService } from "../services/kycService";
import { OnboardingStatus } from "../types/customer.types";
import { KycStatus, OnboardingStep } from "../types/onboarding.types";

export const normalizeKycStatus = (value: string): KycStatus => {
  const normalized = value.trim().toUpperCase().replace(/[-\s]/g, "_");

  if (
    normalized === "PASS" ||
    normalized === "KYC_PASS" ||
    normalized === "KYC_PASSED" ||
    normalized === "KYC_COMPLETED" ||
    normalized === "APPROVED"
  ) {
    return KycStatus.PASS;
  }

  if (
    normalized === "FAIL" ||
    normalized === "FAILED" ||
    normalized === "KYC_FAIL" ||
    normalized === "KYC_FAILED" ||
    normalized === "REJECTED"
  ) {
    return KycStatus.FAIL;
  }

  if (normalized === "REFER") {
    return KycStatus.REFER;
  }

  return KycStatus.KYC_IN_PROGRESS;
};

const resolveKycStatusFromCustomerStatus = (status: OnboardingStatus): KycStatus | null => {
  if (status === OnboardingStatus.KYC_COMPLETED || status === OnboardingStatus.APPROVED || status === OnboardingStatus.MANUAL_APPROVAL_REQUIRED) {
    return KycStatus.PASS;
  }

  if (status === OnboardingStatus.REJECTED) {
    return KycStatus.FAIL;
  }

  return null;
};

export const KycStatusPage = () => {
  const {
    state: { customerId, documentId, kycStatus, correlationId },
    dispatch,
  } = useOnboarding();
  const [isFetchDelayElapsed, setIsFetchDelayElapsed] = useState(false);

  useEffect(() => {
    if (!customerId || !documentId) {
      setIsFetchDelayElapsed(false);
      return;
    }

    setIsFetchDelayElapsed(false);
    const timer = window.setTimeout(() => {
      setIsFetchDelayElapsed(true);
    }, 5_000);

    return () => {
      window.clearTimeout(timer);
    };
  }, [customerId, documentId]);

  const customerStatusQuery = useQuery({
    queryKey: ["customer-status", customerId],
    queryFn: async () => {
      if (!customerId) {
        throw new Error("Customer ID is required for customer status lookup.");
      }

      const customer = await customerService.getCustomer(customerId);
      return customer.status;
    },
    enabled: Boolean(customerId && isFetchDelayElapsed),
    staleTime: 0,
    refetchOnMount: "always",
    refetchInterval: 5_000,
  });

  const query = useQuery({
    queryKey: ["kyc-status", customerId, documentId],
    queryFn: async () => {
      if (!customerId) {
        throw new Error("Customer ID is required for KYC status polling.");
      }

      const result = await kycService.getKycStatus(customerId, documentId ?? undefined);
      const normalizedStatus = normalizeKycStatus(result.status);

      if (normalizedStatus === KycStatus.PASS) {
        return {
          ...result,
          status: normalizedStatus,
        };
      }

      try {
        const customer = await customerService.getCustomer(customerId);
        const statusFromCustomer = resolveKycStatusFromCustomerStatus(customer.status);

        if (statusFromCustomer === KycStatus.PASS) {
          return {
            ...result,
            status: KycStatus.PASS,
          };
        }

        if (statusFromCustomer === KycStatus.FAIL && normalizedStatus !== KycStatus.FAIL) {
          return {
            ...result,
            status: KycStatus.FAIL,
          };
        }
      } catch {
        // Keep KYC polling resilient when customer-service lookup is temporarily unavailable.
      }

      return {
        ...result,
        status: normalizedStatus,
      };
    },
    enabled: Boolean(customerId && documentId && isFetchDelayElapsed),
    staleTime: 0,
    refetchOnMount: "always",
    refetchInterval: 5_000,
  });

  useEffect(() => {
    if (!query.data?.status) {
      return;
    }

    dispatch({
      type: "SET_KYC_STATUS",
      payload: {
        status: query.data.status,
        correlationId: query.data.correlationId,
      },
    });
  }, [dispatch, query.data]);

  const statusFromCustomer = customerStatusQuery.data
    ? resolveKycStatusFromCustomerStatus(customerStatusQuery.data)
    : null;

  const currentStatus = statusFromCustomer ?? query.data?.status ?? kycStatus ?? KycStatus.KYC_IN_PROGRESS;

  if (!isFetchDelayElapsed) {
    return (
      <section data-e2e="kyc-status-delay">
        <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
        <p role="status" className="mt-2 rounded bg-sky-50 p-4 text-sm text-sky-900">
          Preparing latest KYC check. Fetching status in 5 seconds...
        </p>
      </section>
    );
  }

  if (query.isLoading) {
    return <p role="status">Checking KYC status...</p>;
  }

  if (query.error) {
    return (
      <section className="space-y-3">
        <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
        <p className="rounded bg-amber-50 p-4 text-sm text-amber-900" role="alert" data-e2e="kyc-status-error">
          {query.error instanceof Error ? query.error.message : "Unable to fetch KYC status."}
        </p>
      </section>
    );
  }

  if (currentStatus === KycStatus.KYC_IN_PROGRESS) {
    return (
      <section data-e2e="kyc-status-pending">
        <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
        <p className="mt-2 rounded bg-sky-50 p-4 text-sm text-sky-900">Status: Pending. We are polling every 5 seconds.</p>
      </section>
    );
  }

  if (currentStatus === KycStatus.PASS) {
    return (
      <section data-e2e="kyc-status-pass">
        <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
        <p className="mt-2 rounded bg-emerald-50 p-4 text-sm text-emerald-900">Status: Pass. Continue to review your application.</p>
        <button
          type="button"
          className="mt-4 rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700"
          onClick={() => dispatch({ type: "SET_STEP", payload: OnboardingStep.REVIEW })}
          data-e2e="kyc-continue-review"
        >
          Continue to Review
        </button>
      </section>
    );
  }

  if (currentStatus === KycStatus.FAIL) {
    return (
      <section data-e2e="kyc-status-fail">
        <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
        <p className="mt-2 rounded bg-red-50 p-4 text-sm text-red-800">Status: Fail. Your application cannot proceed.</p>
        <button
          type="button"
          className="mt-4 rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700"
          onClick={() => dispatch({ type: "RETRY_DOCUMENT_UPLOAD" })}
          data-e2e="kyc-upload-again"
        >
          Upload document again
        </button>
        {correlationId ? (
          <p className="mt-2 text-xs text-slate-700">Correlation ID: {correlationId}</p>
        ) : null}
      </section>
    );
  }

  return (
    <section data-e2e="kyc-status-refer">
      <h2 className="text-2xl font-bold text-slate-900">Step 3: KYC Verification</h2>
      <p className="mt-2 rounded bg-amber-50 p-4 text-sm text-amber-900">
        Status: Refer. Manual compliance review is required.
      </p>
      {correlationId ? (
        <p className="mt-2 text-xs text-slate-700">Correlation ID: {correlationId}</p>
      ) : null}
    </section>
  );
};
