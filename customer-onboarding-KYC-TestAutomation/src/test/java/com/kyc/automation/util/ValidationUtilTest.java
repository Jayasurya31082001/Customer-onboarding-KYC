package com.kyc.automation.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {

    @Test
    @DisplayName("requireNonNull throws IllegalArgumentException when target object is null")
    void testRequireNonNullWithNull() {
        assertThatThrownBy(() -> ValidationUtil.requireNonNull(null, "paramName", "Context"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[Context] paramName must not be null");
    }

    @Test
    @DisplayName("requireNonNull returns object when target object is non-null")
    void testRequireNonNullWithValidObject() {
        String input = "test";
        String result = ValidationUtil.requireNonNull(input, "paramName", "Context");
        assertThat(result).isEqualTo("test");
    }

    @Test
    @DisplayName("requireNonEmpty throws IllegalArgumentException when string is null or blank")
    void testRequireNonEmptyWithInvalidString() {
        assertThatThrownBy(() -> ValidationUtil.requireNonEmpty((String) null, "strParam"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("strParam must not be null or empty");

        assertThatThrownBy(() -> ValidationUtil.requireNonEmpty("   ", "strParam", "Context"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[Context] strParam must not be null or empty");
    }

    @Test
    @DisplayName("requireNonEmpty returns string when valid")
    void testRequireNonEmptyWithValidString() {
        String result = ValidationUtil.requireNonEmpty("validString", "strParam");
        assertThat(result).isEqualTo("validString");
    }

    @Test
    @DisplayName("requireNonEmpty throws IllegalArgumentException for empty collections and maps")
    void testRequireNonEmptyWithEmptyCollections() {
        assertThatThrownBy(() -> ValidationUtil.requireNonEmpty(Collections.emptyList(), "listParam"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("listParam must not be null or empty");

        assertThatThrownBy(() -> ValidationUtil.requireNonEmpty((List<String>) null, "listParam"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("listParam must not be null or empty");

        assertThatThrownBy(() -> ValidationUtil.requireNonEmpty(Collections.emptyMap(), "mapParam"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mapParam must not be null or empty");
    }

    @Test
    @DisplayName("requireNonEmpty returns collection and map when non-empty")
    void testRequireNonEmptyWithValidCollections() {
        List<String> list = List.of("item");
        Map<String, String> map = Map.of("key", "val");

        assertThat(ValidationUtil.requireNonEmpty(list, "listParam")).isEqualTo(list);
        assertThat(ValidationUtil.requireNonEmpty(map, "mapParam")).isEqualTo(map);
    }
}
