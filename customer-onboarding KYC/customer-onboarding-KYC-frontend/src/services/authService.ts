import type { AuthSession, User } from "../types/auth.types";
import { UserRole } from "../types/auth.types";
import { getOnboardingSessionByEmail } from "./onboardingSessionService";

const AUTH_TOKEN_KEY = "kyc.auth.token";
const AUTH_USER_KEY = "kyc.auth.user";
const REGISTERED_CUSTOMERS_KEY = "kyc.registered.customers";
const LOGIN_ERROR_MESSAGE = "Invalid email or password";
const ONBOARD_FIRST_MESSAGE = "Email not found. Please onboard first.";
const FIXED_PASSWORD = "Welcome@123";
const COMPLIANCE_OFFICER_EMAIL = "compliance.admin@bank.com";
const DEFAULT_CUSTOMER_EMAILS = ["customer@example.com"];

let inMemoryToken: string | null = null;
let inMemoryUser: User | null = null;
let inMemoryRegisteredCustomers: string[] | null = null;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const canUseLocalStorage = (): boolean => {
  if (typeof window === "undefined") {
    return false;
  }

  try {
    return typeof window.localStorage !== "undefined";
  } catch {
    return false;
  }
};

const setStoredToken = (token: string): void => {
  inMemoryToken = token;
  if (canUseLocalStorage()) {
    window.localStorage.setItem(AUTH_TOKEN_KEY, token);
  }
};

const setStoredUser = (user: User): void => {
  inMemoryUser = user;
  if (canUseLocalStorage()) {
    window.localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  }
};

const readRegisteredCustomers = (): string[] => {
  if (inMemoryRegisteredCustomers) {
    return inMemoryRegisteredCustomers;
  }

  if (!canUseLocalStorage()) {
    return DEFAULT_CUSTOMER_EMAILS;
  }

  const raw = window.localStorage.getItem(REGISTERED_CUSTOMERS_KEY);
  if (!raw) {
    return DEFAULT_CUSTOMER_EMAILS;
  }

  try {
    const parsed = JSON.parse(raw) as unknown;
    const emails = Array.isArray(parsed)
      ? parsed.filter((entry): entry is string => typeof entry === "string")
      : [];
    const uniqueEmails = Array.from(new Set([...DEFAULT_CUSTOMER_EMAILS, ...emails]));
    inMemoryRegisteredCustomers = uniqueEmails;
    return uniqueEmails;
  } catch {
    return DEFAULT_CUSTOMER_EMAILS;
  }
};

const setRegisteredCustomers = (emails: string[]): void => {
  inMemoryRegisteredCustomers = emails;

  if (!canUseLocalStorage()) {
    return;
  }

  window.localStorage.setItem(REGISTERED_CUSTOMERS_KEY, JSON.stringify(emails));
};

export const registerCustomerEmail = (email: string): void => {
  const normalizedEmail = email.trim().toLowerCase();
  if (!normalizedEmail) {
    return;
  }

  const existingEmails = readRegisteredCustomers();
  if (existingEmails.includes(normalizedEmail)) {
    return;
  }

  setRegisteredCustomers([...existingEmails, normalizedEmail]);
};

export const isRegisteredCustomerEmail = (email: string): boolean => {
  return readRegisteredCustomers().includes(email.trim().toLowerCase());
};

const clearStoredAuth = (): void => {
  inMemoryToken = null;
  inMemoryUser = null;
  if (canUseLocalStorage()) {
    window.localStorage.removeItem(AUTH_TOKEN_KEY);
    window.localStorage.removeItem(AUTH_USER_KEY);
  }
};

export const getToken = (): string | null => {
  if (inMemoryToken) {
    return inMemoryToken;
  }

  if (!canUseLocalStorage()) {
    return null;
  }

  const storedToken = window.localStorage.getItem(AUTH_TOKEN_KEY);
  inMemoryToken = storedToken;
  return storedToken;
};

export const getStoredUser = (): User | null => {
  if (inMemoryUser) {
    return inMemoryUser;
  }

  if (!canUseLocalStorage()) {
    return null;
  }

  const rawUser = window.localStorage.getItem(AUTH_USER_KEY);
  if (!rawUser) {
    return null;
  }

  try {
    const parsed = JSON.parse(rawUser) as User;
    inMemoryUser = parsed;
    return parsed;
  } catch {
    clearStoredAuth();
    return null;
  }
};

const resolveRole = (email: string): UserRole => {
  const normalizedEmail = email.toLowerCase();
  if (normalizedEmail === COMPLIANCE_OFFICER_EMAIL) {
    return UserRole.COMPLIANCE_OFFICER;
  }
  return UserRole.CUSTOMER;
};

export const login = async (email: string, password: string): Promise<AuthSession> => {
  const trimmedEmail = email.trim();
  const isEmailValid = EMAIL_REGEX.test(trimmedEmail);

  if (!isEmailValid || password !== FIXED_PASSWORD) {
    return Promise.reject(new Error(LOGIN_ERROR_MESSAGE));
  }

  const normalizedEmail = trimmedEmail.toLowerCase();
  const hasSavedOnboardingSession = Boolean(getOnboardingSessionByEmail(trimmedEmail));
  const isKnownCustomer =
    normalizedEmail === COMPLIANCE_OFFICER_EMAIL ||
    isRegisteredCustomerEmail(trimmedEmail) ||
    hasSavedOnboardingSession;

  if (hasSavedOnboardingSession) {
    registerCustomerEmail(trimmedEmail);
  }

  if (!isKnownCustomer) {
    return Promise.reject(new Error(ONBOARD_FIRST_MESSAGE));
  }

  const user: User = {
    id: crypto.randomUUID(),
    email: trimmedEmail,
    roles: [resolveRole(trimmedEmail)],
  };

  const fakeToken = `demo-token-${user.id}`;
  setStoredUser(user);
  setStoredToken(fakeToken);

  return {
    token: fakeToken,
    user,
  };
};

export const logout = (): void => {
  clearStoredAuth();
};

export const resetAuthState = (): void => {
  clearStoredAuth();
  inMemoryRegisteredCustomers = null;

  if (canUseLocalStorage()) {
    window.localStorage.removeItem(REGISTERED_CUSTOMERS_KEY);
  }
};

export const getComplianceOfficerEmail = (): string => COMPLIANCE_OFFICER_EMAIL;
