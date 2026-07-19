package com.example.customerservice.common.validation;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IsoCountryCodeValidatorTest {

    private IsoCountryCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IsoCountryCodeValidator();
    }

    @Test
    @DisplayName("null is valid (deferred to @NotBlank)")
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("blank string is valid (deferred to @NotBlank)")
    void isValid_blank_returnsTrue() {
        assertThat(validator.isValid("  ", null)).isTrue();
    }

    @ParameterizedTest(name = "valid ISO code \"{0}\" is accepted")
    @ValueSource(strings = {"GB", "US", "DE", "FR", "JP"})
    @DisplayName("upper-case ISO 3166-1 alpha-2 codes are valid")
    void isValid_validUpperCaseCodes_returnsTrue(String code) {
        assertThat(validator.isValid(code, null)).isTrue();
    }

    @ParameterizedTest(name = "lower-case code \"{0}\" is accepted")
    @ValueSource(strings = {"gb", "us", "de"})
    @DisplayName("lower-case ISO codes are normalised and accepted")
    void isValid_lowerCaseCodes_returnsTrue(String code) {
        assertThat(validator.isValid(code, null)).isTrue();
    }

    @Test
    @DisplayName("code with surrounding spaces is accepted")
    void isValid_codeWithSpaces_returnsTrue() {
        assertThat(validator.isValid(" GB ", null)).isTrue();
    }

    @ParameterizedTest(name = "invalid code \"{0}\" is rejected")
    @ValueSource(strings = {"XX", "ZZ", "QQ"})
    @DisplayName("non-existent ISO codes are rejected")
    void isValid_nonExistentCodes_returnsFalse(String code) {
        assertThat(validator.isValid(code, null)).isFalse();
    }

    @ParameterizedTest(name = "non-alpha-2 value \"{0}\" is rejected")
    @ValueSource(strings = {"USA", "GBR", "123", "G"})
    @DisplayName("values that are not alpha-2 format are rejected")
    void isValid_nonAlpha2Format_returnsFalse(String code) {
        assertThat(validator.isValid(code, null)).isFalse();
    }
}
