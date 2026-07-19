import { createContext, useCallback, useMemo, useState } from "react";
import type { ReactNode } from "react";
import type { User } from "../types/auth.types";
import { getStoredUser, getToken, login as authLogin, logout as authLogout } from "../services/authService";

export interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(() => getStoredUser());

  const login = useCallback(async (email: string, password: string) => {
    const session = await authLogin(email, password);
    setUser(session.user);
  }, []);

  const logout = useCallback(() => {
    authLogout();
    setUser(null);
  }, []);

  const isAuthenticated = Boolean(getToken() && user);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      login,
      logout,
      isAuthenticated,
    }),
    [isAuthenticated, login, logout, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
