import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useOnboarding } from "../hooks/useOnboarding";
import { getComplianceOfficerEmail } from "../services/authService";
import {
  clearActiveOnboardingEmail,
  hydrateOnboardingStateForEmail,
  resolveOnboardingRouteForEmail,
  setActiveOnboardingEmail,
} from "../services/onboardingSessionService";
import { UserRole } from "../types/auth.types";

interface LoginFormData {
  email: string;
  password: string;
}

export const LoginPage = () => {
  const { login, isAuthenticated, user } = useAuth();
  const { dispatch } = useOnboarding();
  const location = useLocation();
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>();

  useEffect(() => {
    if (!isAuthenticated || !user) {
      return;
    }

    setActiveOnboardingEmail(user.email);

    const restoredState = hydrateOnboardingStateForEmail(user.email);
    if (restoredState) {
      dispatch({ type: "HYDRATE", payload: restoredState });
    } else {
      dispatch({ type: "RESET" });
    }

    if (user.roles.includes(UserRole.COMPLIANCE_OFFICER)) {
      navigate("/compliance", { replace: true });
      return;
    }

    navigate(resolveOnboardingRouteForEmail(user.email), { replace: true });
  }, [dispatch, isAuthenticated, navigate, user]);

  const onSubmit = async (data: LoginFormData): Promise<void> => {
    setSubmitError(null);

    const normalizedEmail = data.email.trim().toLowerCase();
    const normalizedPassword = data.password.trim();

    try {
      await login(normalizedEmail, normalizedPassword);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Invalid email or password");
    }
  };

  const emailErrorId = "email-error";
  const passwordErrorId = "password-error";
  const formErrorId = "login-error";

  const fromPath =
    location.state && typeof location.state === "object" && "from" in location.state
      ? String((location.state as { from?: { pathname?: string } }).from?.pathname ?? "/")
      : "/";

  const handleStartOnboarding = () => {
    clearActiveOnboardingEmail();
    dispatch({ type: "RESET" });
  };

  return (
    <section
      className="mx-auto mt-8 w-full max-w-md rounded-2xl border border-white/20 bg-white/90 p-6 text-slate-900 shadow-[0_24px_80px_rgba(15,23,42,0.25)] backdrop-blur"
      data-e2e="login-page"
      aria-labelledby="login-heading"
    >
      <h1 id="login-heading" className="mb-2 text-3xl font-bold tracking-tight text-slate-900">Sign In</h1>
      <p className="mb-4 text-sm text-slate-700">
        Enter a registered customer email with password.
      </p>
      <p className="mb-4 rounded-lg border border-cyan-100 bg-cyan-50/80 p-2 text-xs font-medium text-cyan-900">
        Compliance account: {getComplianceOfficerEmail()} (redirects to dashboard)
      </p>

      {isAuthenticated && user ? (
        <p role="status" className="mb-4 text-green-700" data-e2e="login-success">
          Signed in as {user.email}
        </p>
      ) : null}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate aria-describedby={submitError ? formErrorId : undefined}>
        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-200"
            aria-invalid={errors.email ? "true" : "false"}
            aria-describedby={errors.email ? emailErrorId : undefined}
            data-e2e="login-email"
            {...register("email", {
              setValueAs: (value) => (typeof value === "string" ? value.trim().toLowerCase() : value),
              required: "Email is required",
              pattern: {
                value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message: "Enter a valid email address",
              },
            })}
          />
          {errors.email ? (
            <p id={emailErrorId} role="alert" className="mt-1 text-sm text-red-600" data-e2e="login-email-error">
              {errors.email.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="password" className="mb-1 block text-sm font-medium">
            Password
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-200"
            aria-invalid={errors.password ? "true" : "false"}
            aria-describedby={errors.password ? passwordErrorId : undefined}
            data-e2e="login-password"
            {...register("password", {
              setValueAs: (value) => (typeof value === "string" ? value.trim() : value),
              required: "Password is required",
            })}
          />
          {errors.password ? (
            <p id={passwordErrorId} role="alert" className="mt-1 text-sm text-red-600" data-e2e="login-password-error">
              {errors.password.message}
            </p>
          ) : null}
        </div>

        {submitError ? (
          <p id={formErrorId} role="alert" className="text-sm text-red-700" data-e2e="login-form-error">
            {submitError}
          </p>
        ) : null}

        <button
          type="submit"
          className="w-full rounded-lg bg-gradient-to-r from-cyan-500 to-emerald-500 px-4 py-2 font-semibold text-slate-950 transition hover:brightness-110 disabled:opacity-60"
          disabled={isSubmitting}
          data-e2e="login-submit"
        >
          {isSubmitting ? "Signing in..." : "Sign in"}
        </button>
      </form>

      <p className="mt-4 text-xs text-slate-700">
        Protected destination: {fromPath}. New users should{" "}
        <Link to="/onboarding" className="font-semibold text-cyan-700 underline decoration-cyan-500/70 underline-offset-2" onClick={handleStartOnboarding}>
          Onboard
        </Link>{" "}
        first.
      </p>
    </section>
  );
};
