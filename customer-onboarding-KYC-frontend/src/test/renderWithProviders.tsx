import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, type RenderOptions } from "@testing-library/react";
import type { ReactElement, ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../context/AuthContext";
import { OnboardingProvider } from "../context/OnboardingContext";

interface ProviderProps {
  children: ReactNode;
  initialRoute?: string;
}

const Providers = ({ children, initialRoute = "/" }: ProviderProps) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialRoute]}>
        <AuthProvider>
          <OnboardingProvider>{children}</OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
};

export const renderWithProviders = (
  ui: ReactElement,
  options?: Omit<RenderOptions, "wrapper"> & { initialRoute?: string },
) => {
  const { initialRoute, ...rest } = options ?? {};

  return render(ui, {
    wrapper: ({ children }) => <Providers initialRoute={initialRoute}>{children}</Providers>,
    ...rest,
  });
};
