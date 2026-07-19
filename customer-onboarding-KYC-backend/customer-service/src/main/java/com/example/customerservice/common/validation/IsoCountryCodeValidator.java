package com.example.customerservice.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class IsoCountryCodeValidator implements ConstraintValidator<IsoCountryCode, String> {

    private static final Set<String> ISO_COUNTRIES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        return ISO_COUNTRIES.contains(value.trim().toUpperCase(Locale.ROOT));
    }
}

