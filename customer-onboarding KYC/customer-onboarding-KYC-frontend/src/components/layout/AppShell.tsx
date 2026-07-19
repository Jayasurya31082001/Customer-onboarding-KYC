import { Link, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useOnboarding } from "../../hooks/useOnboarding";
import { clearActiveOnboardingEmail } from "../../services/onboardingSessionService";
import { UserRole } from "../../types/auth.types";

export const AppShell = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { dispatch } = useOnboarding();
  const navigate = useNavigate();
  const currentYear = new Date().getFullYear();
  const isComplianceUser = Boolean(user?.roles.includes(UserRole.COMPLIANCE_OFFICER));
  const containerClass = "mx-auto w-full max-w-screen-2xl px-4 sm:px-6 lg:px-8 2xl:px-10";

  const handleLogout = () => {
    logout();
    clearActiveOnboardingEmail();
    dispatch({ type: "RESET" });
    navigate("/login", { replace: true });
  };

  return (
    <div className="relative flex min-h-screen min-h-[100svh] min-h-[100dvh] flex-col overflow-x-hidden bg-slate-950 text-slate-100">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_0%,rgba(56,189,248,0.28),transparent_35%),radial-gradient(circle_at_80%_20%,rgba(34,197,94,0.24),transparent_32%),radial-gradient(circle_at_50%_100%,rgba(251,191,36,0.22),transparent_38%)]"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -left-24 top-24 h-64 w-64 rounded-full bg-cyan-400/20 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -right-20 top-20 h-72 w-72 rounded-full bg-emerald-300/20 blur-3xl"
      />

      <header className="relative border-b border-white/15 bg-slate-900/65 backdrop-blur-xl">
        <div className={`${containerClass} flex flex-wrap items-center justify-between gap-3 py-4`}>
          <Link to="/onboarding" className="text-xl font-bold tracking-tight text-white">
            Customer Onboarding KYC
          </Link>
          <nav className="flex flex-wrap items-center justify-end gap-2 text-sm sm:gap-3">
            {isAuthenticated ? (
              <>
                {user?.email ? (
                  <span
                    className="rounded-full border border-white/20 bg-white/10 px-3 py-1.5 text-xs font-medium text-slate-100"
                    data-e2e="header-user-email"
                  >
                    Signed in: {user.email}
                  </span>
                ) : null}
                {!isComplianceUser ? (
                  <Link
                    to="/onboarding"
                    className="rounded-full border border-white/15 bg-white/10 px-3 py-1.5 font-medium text-white transition hover:bg-white/20"
                    data-e2e="nav-onboarding"
                  >
                    Onboarding
                  </Link>
                ) : null}
                {isComplianceUser ? (
                  <Link
                    to="/compliance"
                    className="rounded-full border border-white/15 bg-white/10 px-3 py-1.5 font-medium text-white transition hover:bg-white/20"
                    data-e2e="nav-compliance"
                  >
                    Compliance Dashboard
                  </Link>
                ) : null}
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-full bg-gradient-to-r from-cyan-400 to-emerald-400 px-3.5 py-1.5 font-semibold text-slate-900 transition hover:brightness-110"
                  data-e2e="logout-button"
                >
                  Log out
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="rounded-full border border-white/15 bg-white/10 px-3 py-1.5 font-medium text-white transition hover:bg-white/20"
              >
                Login
              </Link>
            )}
          </nav>
        </div>
      </header>

      <main className={`${containerClass} relative flex flex-1 flex-col py-6 sm:py-8`}>
        <Outlet />
      </main>

      <footer className="relative border-t border-white/15 bg-slate-900/65 backdrop-blur-xl">
        <div className={`${containerClass} flex flex-col gap-3 py-5 text-sm text-slate-200 sm:flex-row sm:items-center sm:justify-between`}>
          <p>Secure onboarding journey for customers and compliance teams.</p>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-300">
            <span>Fast KYC checks</span>
            <span>Real-time status</span>
            <span>{currentYear} Deloitte</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
