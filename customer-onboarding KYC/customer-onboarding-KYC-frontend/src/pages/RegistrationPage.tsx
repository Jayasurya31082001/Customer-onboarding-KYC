import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useAuth } from "../hooks/useAuth";
import { useOnboarding } from "../hooks/useOnboarding";
import { customerService } from "../services/customerService";
import { registerCustomerEmail } from "../services/authService";
import { upsertOnboardingSession } from "../services/onboardingSessionService";
import { OnboardingStatus, type PersonalDetailsForm } from "../types/customer.types";
import { OnboardingStep } from "../types/onboarding.types";

const monthOptions = [
  { value: "01", label: "January" },
  { value: "02", label: "February" },
  { value: "03", label: "March" },
  { value: "04", label: "April" },
  { value: "05", label: "May" },
  { value: "06", label: "June" },
  { value: "07", label: "July" },
  { value: "08", label: "August" },
  { value: "09", label: "September" },
  { value: "10", label: "October" },
  { value: "11", label: "November" },
  { value: "12", label: "December" },
];

const pad2 = (value: number): string => value.toString().padStart(2, "0");

const personalDetailsSchema = z.object({
  firstName: z
    .string()
    .trim()
    .min(2, "First name must be between 2 and 50 characters")
    .max(50, "First name must be between 2 and 50 characters")
    .regex(/^[A-Za-z]{2,50}$/, "First name must contain letters only"),
  lastName: z
    .string()
    .trim()
    .min(2, "Last name must be between 2 and 50 characters")
    .max(50, "Last name must be between 2 and 50 characters")
    .regex(/^[A-Za-z]{2,50}$/, "Last name must contain letters only"),
  email: z.string().trim().toLowerCase().email("Enter a valid email"),
  dateOfBirth: z
    .string()
    .refine((value) => {
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return false;
      }
      const today = new Date();
      const adultThreshold = new Date(
        today.getFullYear() - 18,
        today.getMonth(),
        today.getDate(),
      );
      return date <= adultThreshold;
    }, "You must be at least 18 years old"),
  phoneNumber: z.string().trim().regex(/^\+[1-9]\d{1,14}$/, "Phone number must be in E.164 format"),
  nationality: z.string().trim().toUpperCase().regex(/^[A-Z]{2}$/, "Nationality must be an ISO 3166-1 alpha-2 code"),
  addressLine1: z.string().trim().min(1, "Address line is required").max(100, "Address line must be at most 100 characters"),
  city: z.string().trim().min(1, "City is required").max(50, "City must be at most 50 characters"),
  postcode: z
    .string()
    .trim()
    .toUpperCase()
    .regex(
      /^(GIR 0AA|(?:(?:[A-Z][0-9]{1,2}|[A-Z][A-HJK-Y][0-9]{1,2}|[A-Z][0-9][A-Z]|[A-Z][A-HJK-Y][0-9][ABEHMNPRVWXY]) ?[0-9][ABD-HJLNP-UW-Z]{2}))$/,
      "Postcode must be a valid UK postcode",
    ),
});

export const RegistrationPage = () => {
  const { login } = useAuth();
  const { dispatch } = useOnboarding();
  const [formError, setFormError] = useState<string | null>(null);
  const [dobDay, setDobDay] = useState("");
  const [dobMonth, setDobMonth] = useState("");
  const [dobYear, setDobYear] = useState("");

  const moveToDocumentUpload = (customerId: string, values: PersonalDetailsForm) => {
    dispatch({
      type: "SET_PERSONAL_DETAILS",
      payload: {
        customerId,
        details: values,
        status: OnboardingStatus.PENDING,
      },
    });
  };

  const {
    register,
    handleSubmit,
    setValue,
    clearErrors,
    formState: { errors, isSubmitting },
  } = useForm<PersonalDetailsForm>({
    resolver: zodResolver(personalDetailsSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      dateOfBirth: "",
      phoneNumber: "",
      nationality: "GB",
      addressLine1: "",
      city: "",
      postcode: "",
    },
  });

  const yearOptions = useMemo(() => {
    const currentYear = new Date().getFullYear();
    const youngestYear = currentYear - 18;
    const oldestYear = currentYear - 110;

    return Array.from({ length: youngestYear - oldestYear + 1 }, (_, index) => String(youngestYear - index));
  }, []);

  const dayOptions = useMemo(() => {
    if (!dobYear || !dobMonth) {
      return Array.from({ length: 31 }, (_, index) => pad2(index + 1));
    }

    const daysInMonth = new Date(Number(dobYear), Number(dobMonth), 0).getDate();
    return Array.from({ length: daysInMonth }, (_, index) => pad2(index + 1));
  }, [dobMonth, dobYear]);

  useEffect(() => {
    if (!dobDay || !dobMonth || !dobYear) {
      setValue("dateOfBirth", "", { shouldValidate: false, shouldDirty: false, shouldTouch: false });
      clearErrors("dateOfBirth");
      return;
    }

    const formattedDate = `${dobYear}-${dobMonth}-${dobDay}`;
    setValue("dateOfBirth", formattedDate, { shouldValidate: true, shouldDirty: true, shouldTouch: true });
  }, [clearErrors, dobDay, dobMonth, dobYear, setValue]);

  useEffect(() => {
    if (!dobDay || !dobMonth || !dobYear) {
      return;
    }

    const maxDay = new Date(Number(dobYear), Number(dobMonth), 0).getDate();
    if (Number(dobDay) > maxDay) {
      setDobDay(pad2(maxDay));
    }
  }, [dobDay, dobMonth, dobYear]);

  const onSubmit = async (values: PersonalDetailsForm) => {
    setFormError(null);

    try {
      const response = await customerService.registerCustomer(values);
      registerCustomerEmail(values.email);
      await login(values.email, "Welcome@123");
      upsertOnboardingSession(values.email, response.customerId, {
        currentStep: OnboardingStep.DOCUMENT_UPLOAD,
        personalDetails: values,
        documentId: null,
        documentsUploaded: false,
        kycStatus: null,
        applicationStatus: response.status,
        correlationId: null,
      });
      dispatch({
        type: "SET_PERSONAL_DETAILS",
        payload: {
          customerId: response.customerId,
          details: values,
          status: response.status,
        },
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to register customer";

      if (message.toLowerCase().includes("network")) {
        // Demo fallback: continue onboarding with a local customer id when backend is unavailable.
        registerCustomerEmail(values.email);
        const fallbackCustomerId = crypto.randomUUID();
        await login(values.email, "Welcome@123");
        upsertOnboardingSession(values.email, fallbackCustomerId, {
          currentStep: OnboardingStep.DOCUMENT_UPLOAD,
          personalDetails: values,
          documentId: null,
          documentsUploaded: false,
          kycStatus: null,
          applicationStatus: OnboardingStatus.PENDING,
          correlationId: null,
        });
        moveToDocumentUpload(fallbackCustomerId, values);
        setFormError("Backend unreachable. Proceeding in demo mode with a local customer profile.");
        return;
      }

      setFormError(message);
    }
  };

  const fieldClass =
    "mt-1 w-full rounded-lg border border-slate-400 bg-white/95 px-3 py-2 text-slate-900 placeholder:text-slate-500 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-200";

  return (
    <section aria-labelledby="personal-details-heading" data-e2e="registration-page">
      <h2 id="personal-details-heading" className="text-2xl font-bold text-slate-900">
        Step 1: Personal Details
      </h2>
      <p className="mt-1 text-sm text-slate-700">All fields must match the customer-service validation rules.</p>

      <form className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div>
          <label htmlFor="firstName" className="text-sm font-semibold text-slate-800">
            First Name
          </label>
          <input id="firstName" className={fieldClass} aria-describedby={errors.firstName ? "firstName-error" : undefined} {...register("firstName")} />
          {errors.firstName ? (
            <p id="firstName-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.firstName.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="lastName" className="text-sm font-semibold text-slate-800">
            Last Name
          </label>
          <input id="lastName" className={fieldClass} aria-describedby={errors.lastName ? "lastName-error" : undefined} {...register("lastName")} />
          {errors.lastName ? (
            <p id="lastName-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.lastName.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="email" className="text-sm font-semibold text-slate-800">
            Email
          </label>
          <input id="email" type="email" className={fieldClass} aria-describedby={errors.email ? "email-error" : undefined} {...register("email")} />
          {errors.email ? (
            <p id="email-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.email.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="dob-day" className="text-sm font-semibold text-slate-800">
            Date Of Birth
          </label>
          <div className="mt-1 grid grid-cols-3 gap-2" aria-describedby={errors.dateOfBirth ? "dob-error" : "dob-help"}>
            <select
              id="dob-day"
              className="w-full rounded-lg border border-slate-400 bg-white/95 px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-200"
              value={dobDay}
              onChange={(event) => setDobDay(event.target.value)}
            >
              <option value="">Day</option>
              {dayOptions.map((day) => (
                <option key={day} value={day}>
                  {day}
                </option>
              ))}
            </select>
            <select
              id="dob-month"
              aria-label="Month"
              className="w-full rounded-lg border border-slate-400 bg-white/95 px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-200"
              value={dobMonth}
              onChange={(event) => setDobMonth(event.target.value)}
            >
              <option value="">Month</option>
              {monthOptions.map((month) => (
                <option key={month.value} value={month.value}>
                  {month.label}
                </option>
              ))}
            </select>
            <select
              id="dob-year"
              aria-label="Year"
              className="w-full rounded-lg border border-slate-400 bg-white/95 px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-200"
              value={dobYear}
              onChange={(event) => setDobYear(event.target.value)}
            >
              <option value="">Year</option>
              {yearOptions.map((year) => (
                <option key={year} value={year}>
                  {year}
                </option>
              ))}
            </select>
          </div>
          <input type="hidden" {...register("dateOfBirth")} />
          <p id="dob-help" className="mt-1 text-xs text-slate-700">
            Select Day, Month, and Year.
          </p>
          {errors.dateOfBirth ? (
            <p id="dob-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.dateOfBirth.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="phoneNumber" className="text-sm font-semibold text-slate-800">
            Phone Number
          </label>
          <input id="phoneNumber" placeholder="+447911123456" className={fieldClass} aria-describedby={errors.phoneNumber ? "phone-error" : undefined} {...register("phoneNumber")} />
          {errors.phoneNumber ? (
            <p id="phone-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.phoneNumber.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="nationality" className="text-sm font-semibold text-slate-800">
            Nationality (ISO code)
          </label>
          <input id="nationality" className={fieldClass} maxLength={2} aria-describedby={errors.nationality ? "nationality-error" : undefined} {...register("nationality")} />
          {errors.nationality ? (
            <p id="nationality-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.nationality.message}
            </p>
          ) : null}
        </div>

        <div className="md:col-span-2">
          <label htmlFor="addressLine1" className="text-sm font-semibold text-slate-800">
            Address Line 1
          </label>
          <input id="addressLine1" className={fieldClass} aria-describedby={errors.addressLine1 ? "address-error" : undefined} {...register("addressLine1")} />
          {errors.addressLine1 ? (
            <p id="address-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.addressLine1.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="city" className="text-sm font-semibold text-slate-800">
            City
          </label>
          <input id="city" className={fieldClass} aria-describedby={errors.city ? "city-error" : undefined} {...register("city")} />
          {errors.city ? (
            <p id="city-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.city.message}
            </p>
          ) : null}
        </div>

        <div>
          <label htmlFor="postcode" className="text-sm font-semibold text-slate-800">
            UK Postcode
          </label>
          <input id="postcode" className={fieldClass} aria-describedby={errors.postcode ? "postcode-error" : undefined} {...register("postcode")} />
          {errors.postcode ? (
            <p id="postcode-error" className="mt-1 text-sm text-red-700" role="alert">
              {errors.postcode.message}
            </p>
          ) : null}
        </div>

        {formError ? (
          <p role="alert" className="md:col-span-2 text-sm text-red-700" data-e2e="registration-error">
            {formError}
          </p>
        ) : null}

        <div className="md:col-span-2">
          <button
            type="submit"
            className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700 disabled:opacity-60"
            disabled={isSubmitting}
            data-e2e="registration-submit"
          >
            {isSubmitting ? "Registering..." : "Register Customer"}
          </button>
        </div>
      </form>
    </section>
  );
};
