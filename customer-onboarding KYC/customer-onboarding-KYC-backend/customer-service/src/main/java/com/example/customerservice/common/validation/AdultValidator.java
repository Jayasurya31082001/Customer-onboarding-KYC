package com.example.customerservice.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private int minYears;

    @Override
    public void initialize(Adult constraintAnnotation) {
        this.minYears = constraintAnnotation.minYears();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return !value.isAfter(LocalDate.now().minusYears(minYears));
    }
}

