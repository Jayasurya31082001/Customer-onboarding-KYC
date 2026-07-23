package com.kyc.automation.util;

import java.util.Collection;
import java.util.Map;

/**
 * Centralized utility class for parameter validation across API client and service classes.
 * Throws {@link IllegalArgumentException} with explicit error messages identifying parameter names
 * and calling context when validation fails.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class
    }

    /**
     * Validates that the target object parameter is not null.
     *
     * @param param     the object to check
     * @param paramName the parameter name for error reporting
     * @param <T>       the object type
     * @return the non-null parameter
     * @throws IllegalArgumentException if param is null
     */
    public static <T> T requireNonNull(T param, String paramName) {
        if (param == null) {
            throw new IllegalArgumentException(paramName + " must not be null");
        }
        return param;
    }

    /**
     * Validates that the target object parameter is not null with contextual details.
     *
     * @param param     the object to check
     * @param paramName the parameter name for error reporting
     * @param context   class or method context for error reporting
     * @param <T>       the object type
     * @return the non-null parameter
     * @throws IllegalArgumentException if param is null
     */
    public static <T> T requireNonNull(T param, String paramName, String context) {
        if (param == null) {
            throw new IllegalArgumentException(String.format("[%s] %s must not be null", context, paramName));
        }
        return param;
    }

    /**
     * Validates that the target String parameter is not null and not blank/empty.
     *
     * @param param     the String to check
     * @param paramName the parameter name for error reporting
     * @return the non-blank String
     * @throws IllegalArgumentException if param is null or blank
     */
    public static String requireNonEmpty(String param, String paramName) {
        if (param == null || param.isBlank()) {
            throw new IllegalArgumentException(paramName + " must not be null or empty");
        }
        return param;
    }

    /**
     * Validates that the target String parameter is not null and not blank/empty with contextual details.
     *
     * @param param     the String to check
     * @param paramName the parameter name for error reporting
     * @param context   class or method context for error reporting
     * @return the non-blank String
     * @throws IllegalArgumentException if param is null or blank
     */
    public static String requireNonEmpty(String param, String paramName, String context) {
        if (param == null || param.isBlank()) {
            throw new IllegalArgumentException(String.format("[%s] %s must not be null or empty", context, paramName));
        }
        return param;
    }

    /**
     * Validates that the target Collection parameter is not null and contains at least one element.
     *
     * @param collection the collection to check
     * @param paramName  the parameter name for error reporting
     * @param <T>        the collection type
     * @return the non-empty collection
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String paramName) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(paramName + " must not be null or empty");
        }
        return collection;
    }

    /**
     * Validates that the target Collection parameter is not null and contains at least one element with context.
     *
     * @param collection the collection to check
     * @param paramName  the parameter name for error reporting
     * @param context    class or method context for error reporting
     * @param <T>        the collection type
     * @return the non-empty collection
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String paramName, String context) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(String.format("[%s] %s must not be null or empty", context, paramName));
        }
        return collection;
    }

    /**
     * Validates that the target Map parameter is not null and contains at least one key-value pair.
     *
     * @param map       the map to check
     * @param paramName the parameter name for error reporting
     * @param <K>       key type
     * @param <V>       value type
     * @return the non-empty map
     * @throws IllegalArgumentException if map is null or empty
     */
    public static <K, V> Map<K, V> requireNonEmpty(Map<K, V> map, String paramName) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(paramName + " must not be null or empty");
        }
        return map;
    }

    /**
     * Validates that the target Map parameter is not null and contains at least one key-value pair with context.
     *
     * @param map       the map to check
     * @param paramName the parameter name for error reporting
     * @param context   class or method context for error reporting
     * @param <K>       key type
     * @param <V>       value type
     * @return the non-empty map
     * @throws IllegalArgumentException if map is null or empty
     */
    public static <K, V> Map<K, V> requireNonEmpty(Map<K, V> map, String paramName, String context) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(String.format("[%s] %s must not be null or empty", context, paramName));
        }
        return map;
    }

    /**
     * Validates that the target byte array parameter is not null and non-empty.
     *
     * @param array     the byte array to check
     * @param paramName the parameter name for error reporting
     * @return the non-empty byte array
     * @throws IllegalArgumentException if array is null or empty
     */
    public static byte[] requireNonEmpty(byte[] array, String paramName) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(paramName + " must not be null or empty");
        }
        return array;
    }

    /**
     * Validates that the target byte array parameter is not null and non-empty with context.
     *
     * @param array     the byte array to check
     * @param paramName the parameter name for error reporting
     * @param context   class or method context for error reporting
     * @return the non-empty byte array
     * @throws IllegalArgumentException if array is null or empty
     */
    public static byte[] requireNonEmpty(byte[] array, String paramName, String context) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(String.format("[%s] %s must not be null or empty", context, paramName));
        }
        return array;
    }
}
