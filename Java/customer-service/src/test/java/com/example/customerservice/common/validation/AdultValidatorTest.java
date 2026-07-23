package com.example.customerservice.common.validation;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdultValidatorTest {

    private AdultValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AdultValidator();
        ReflectionTestUtils.setField(validator, "minYears", 18);
    }

    @Test
    @DisplayName("null date is valid (deferred to @NotNull)")
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("exactly 18 years old today is valid")
    void isValid_exactlyEighteenYears_returnsTrue() {
        LocalDate eighteenthBirthday = LocalDate.now().minusYears(18);
        assertThat(validator.isValid(eighteenthBirthday, null)).isTrue();
    }

    @Test
    @DisplayName("one day short of 18 years is invalid")
    void isValid_oneDayShortOfEighteen_returnsFalse() {
        LocalDate oneDayShort = LocalDate.now().minusYears(18).plusDays(1);
        assertThat(validator.isValid(oneDayShort, null)).isFalse();
    }

    @Test
    @DisplayName("65 years old is valid")
    void isValid_sixtyFiveYearsOld_returnsTrue() {
        LocalDate dob = LocalDate.now().minusYears(65);
        assertThat(validator.isValid(dob, null)).isTrue();
    }

    @Test
    @DisplayName("future date is invalid")
    void isValid_futureDate_returnsFalse() {
        assertThat(validator.isValid(LocalDate.now().plusDays(1), null)).isFalse();
    }

    @Test
    @DisplayName("today's date is invalid (age = 0)")
    void isValid_today_returnsFalse() {
        assertThat(validator.isValid(LocalDate.now(), null)).isFalse();
    }

}
