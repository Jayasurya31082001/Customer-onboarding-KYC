import "@testing-library/jest-dom/vitest";
import { afterEach } from "vitest";
import { resetAuthState } from "../services/authService";
import { resetOnboardingSessionState } from "../services/onboardingSessionService";

afterEach(() => {
	resetAuthState();
	resetOnboardingSessionState();

	try {
		window.localStorage?.clear();
	} catch {
		// Local storage may be unavailable in some test runtimes.
	}
});
