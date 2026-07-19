import { Navigate, Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./components/common/ProtectedRoute";
import { AppShell } from "./components/layout/AppShell";
import { WizardLayout } from "./components/layout/WizardLayout";
import { UserRole } from "./types/auth.types";
import { LoginPage } from "./pages/LoginPage";
import { OnboardingFlowPage } from "./pages/OnboardingFlowPage";
import { ComplianceDashboard } from "./pages/ComplianceDashboard";
import { AccountConfirmationPage } from "./pages/AccountConfirmationPage";

function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/onboarding" element={<WizardLayout />}>
          <Route index element={<OnboardingFlowPage />} />
          <Route path="confirmation" element={<AccountConfirmationPage />} />
        </Route>

        <Route element={<ProtectedRoute requiredRole={UserRole.COMPLIANCE_OFFICER} />}>
          <Route path="/compliance" element={<ComplianceDashboard />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/onboarding" replace />} />
    </Routes>
  );
}

export default App;
