package com.example.customerservice.customer.dto;

import com.example.customerservice.common.validation.Adult;
import com.example.customerservice.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @Schema(description = "Customer first name containing letters only",
                example = "Alice",
                minLength = 2,
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "firstName is required")
        @Size(min = 2, max = 50, message = "firstName must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]{2,50}$", message = "firstName must contain letters only")
        String firstName,

        @Schema(description = "Customer last name containing letters only",
                example = "Walker",
                minLength = 2,
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "lastName is required")
        @Size(min = 2, max = 50, message = "lastName must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]{2,50}$", message = "lastName must contain letters only")
        String lastName,

        @Schema(description = "Unique email address for the customer",
                example = "alice.walker@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @Schema(description = "Customer date of birth. Must be at least 18 years in the past",
                example = "1990-02-14",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Adult(message = "dateOfBirth must be for an adult aged 18 or above")
        LocalDate dateOfBirth,

        @Schema(description = "Customer phone number in E.164 format",
                example = "+447911123456",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "phoneNumber is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phoneNumber must be in E.164 format")
        String phoneNumber,

        @Schema(description = "ISO 3166-1 alpha-2 nationality code",
                example = "GB",
                minLength = 2,
                maxLength = 2,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "nationality is required")
        @IsoCountryCode
        String nationality,

        @Schema(description = "Primary address line",
                example = "221B Baker Street",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "addressLine1 is required")
        @Size(max = 100, message = "addressLine1 must be at most 100 characters")
        String addressLine1,

        @Schema(description = "Customer city",
                example = "London",
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "city is required")
        @Size(max = 50, message = "city must be at most 50 characters")
        String city,

        @Schema(description = "UK postcode",
                example = "SW1A 1AA",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "postcode is required")
        @Pattern(
                regexp = "^(GIR 0AA|(?:(?:[A-Z][0-9]{1,2}|[A-Z][A-HJK-Y][0-9]{1,2}|[A-Z][0-9][A-Z]|[A-Z][A-HJK-Y][0-9][ABEHMNPRVWXY]) ?[0-9][ABD-HJLNP-UW-Z]{2}))$",
                message = "postcode must be a valid UK postcode"
        )
        String postcode
) {
}

