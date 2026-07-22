package com.kyc.automation.util;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Utility class for reusable REST-assured request building, execution, header handling,
 * and response status validation across API client classes.
 */
public final class ApiClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ApiClientUtil.class);

    private ApiClientUtil() {
        // Utility class – non-instantiable
    }

    /**
     * Resolves a correlation ID, returning the provided ID if non-null and non-blank,
     * or generating a random UUID string if empty or null.
     *
     * @param correlationId optional correlation header value
     * @return non-blank correlation ID string
     */
    public static String resolveCorrelationId(String correlationId) {
        return (correlationId != null && !correlationId.isBlank()) ? correlationId : UUID.randomUUID().toString();
    }

    /**
     * Builds default headers including optional authorization token and default Content-Type header.
     *
     * @param authToken optional authorization token (may be null or blank)
     * @return Headers object containing default request headers
     */
    public static Headers buildDefaultHeaders(String authToken) {
        if (authToken != null && !authToken.isBlank()) {
            return new Headers(
                    new Header("Authorization", "Bearer " + authToken),
                    new Header("Content-Type", ContentType.JSON.toString())
            );
        }
        return new Headers(new Header("Content-Type", ContentType.JSON.toString()));
    }

    /**
     * Executes a POST request with a given request specification, endpoint, and body payload.
     *
     * @param spec     the REST-assured RequestSpecification
     * @param endpoint target endpoint path
     * @param body     body payload object
     * @return the raw RestAssured Response
     */
    public static Response executePost(RequestSpecification spec, String endpoint, Object body) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executePost");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executePost");
        ValidationUtil.requireNonNull(body, "body", "ApiClientUtil.executePost");

        LOG.debug("Sending POST request to endpoint: {}", endpoint);
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post(endpoint);
    }

    /**
     * Executes a GET request with optional path parameters.
     *
     * @param spec       the REST-assured RequestSpecification
     * @param endpoint   target endpoint path or template
     * @param pathParams optional path parameters to populate in template
     * @return the raw RestAssured Response
     */
    public static Response executeGet(RequestSpecification spec, String endpoint, Object... pathParams) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executeGet");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executeGet");

        LOG.debug("Sending GET request to endpoint: {}", endpoint);
        if (pathParams != null && pathParams.length > 0) {
            return given()
                    .spec(spec)
                    .when()
                    .get(endpoint, pathParams);
        } else {
            return given()
                    .spec(spec)
                    .when()
                    .get(endpoint);
        }
    }

    /**
     * Executes a GET request with an explicit Accept header.
     *
     * @param spec         the REST-assured RequestSpecification
     * @param acceptHeader Accept header string (e.g. "application/json")
     * @param endpoint     target endpoint path template
     * @param pathParams   optional path parameters
     * @return the raw RestAssured Response
     */
    public static Response executeGetWithAccept(RequestSpecification spec, String acceptHeader, String endpoint, Object... pathParams) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executeGetWithAccept");
        ValidationUtil.requireNonEmpty(acceptHeader, "acceptHeader", "ApiClientUtil.executeGetWithAccept");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executeGetWithAccept");

        LOG.debug("Sending GET request with accept '{}' to endpoint: {}", acceptHeader, endpoint);
        return given()
                .spec(spec)
                .accept(acceptHeader)
                .when()
                .get(endpoint, pathParams);
    }

    /**
     * Executes a GET request with a query parameter.
     *
     * @param spec       the REST-assured RequestSpecification
     * @param endpoint   target endpoint path
     * @param paramName  query parameter key name
     * @param paramValue query parameter value
     * @return the raw RestAssured Response
     */
    public static Response executeGetWithQueryParam(RequestSpecification spec, String endpoint, String paramName, String paramValue) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executeGetWithQueryParam");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executeGetWithQueryParam");
        ValidationUtil.requireNonEmpty(paramName, "paramName", "ApiClientUtil.executeGetWithQueryParam");
        ValidationUtil.requireNonEmpty(paramValue, "paramValue", "ApiClientUtil.executeGetWithQueryParam");

        LOG.debug("Sending GET request to endpoint: {} with query parameter {}={}", endpoint, paramName, paramValue);
        return given()
                .spec(spec)
                .queryParam(paramName, paramValue)
                .when()
                .get(endpoint);
    }

    /**
     * Executes a PATCH request with body and optional path parameters.
     *
     * @param spec       the REST-assured RequestSpecification
     * @param endpoint   target endpoint path template
     * @param body       payload object
     * @param pathParams optional path parameters
     * @return the raw RestAssured Response
     */
    public static Response executePatch(RequestSpecification spec, String endpoint, Object body, Object... pathParams) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executePatch");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executePatch");
        ValidationUtil.requireNonNull(body, "body", "ApiClientUtil.executePatch");

        LOG.debug("Sending PATCH request to endpoint: {}", endpoint);
        if (pathParams != null && pathParams.length > 0) {
            return given()
                    .spec(spec)
                    .body(body)
                    .when()
                    .patch(endpoint, pathParams);
        } else {
            return given()
                    .spec(spec)
                    .body(body)
                    .when()
                    .patch(endpoint);
        }
    }

    /**
     * Executes a multipart POST request (for file upload endpoints).
     *
     * @param spec                 the REST-assured RequestSpecification
     * @param endpoint             target endpoint path
     * @param queryParamName       query parameter key name
     * @param queryParamValue      query parameter value
     * @param multiPartControlName form multipart field name
     * @param file                 the File to attach
     * @param mimeType             MIME content-type for the attachment part
     * @return the raw RestAssured Response
     */
    public static Response executeMultipartPost(RequestSpecification spec,
                                                 String endpoint,
                                                 String queryParamName,
                                                 String queryParamValue,
                                                 String multiPartControlName,
                                                 File file,
                                                 String mimeType) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonEmpty(queryParamName, "queryParamName", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonEmpty(queryParamValue, "queryParamValue", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonEmpty(multiPartControlName, "multiPartControlName", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonNull(file, "file", "ApiClientUtil.executeMultipartPost");
        ValidationUtil.requireNonEmpty(mimeType, "mimeType", "ApiClientUtil.executeMultipartPost");

        LOG.debug("Sending Multipart POST request to endpoint: {} with file {}", endpoint, file.getName());
        return given()
                .spec(spec)
                .queryParam(queryParamName, queryParamValue)
                .multiPart(multiPartControlName, file, mimeType)
                .when()
                .post(endpoint);
    }

    /**
     * Generic request execution helper supporting any HTTP Method.
     *
     * @param spec     the REST-assured RequestSpecification
     * @param method   the HTTP Method (e.g. GET, POST, PUT, DELETE)
     * @param endpoint target endpoint path
     * @param body     optional body payload
     * @return the raw RestAssured Response
     */
    public static Response executeRequest(RequestSpecification spec, Method method, String endpoint, Object body) {
        ValidationUtil.requireNonNull(spec, "spec", "ApiClientUtil.executeRequest");
        ValidationUtil.requireNonNull(method, "method", "ApiClientUtil.executeRequest");
        ValidationUtil.requireNonEmpty(endpoint, "endpoint", "ApiClientUtil.executeRequest");

        LOG.debug("Sending {} request to endpoint: {}", method, endpoint);
        var req = given().spec(spec);
        if (body != null) {
            req.body(body);
        }
        return req.when().request(method, endpoint);
    }

    /**
     * Validates that the response status code matches the expected HTTP status code.
     *
     * @param response       the RestAssured Response
     * @param expectedStatus expected HTTP status code
     * @throws AssertionError if the actual status code differs from expected
     */
    public static void validateResponseStatus(Response response, int expectedStatus) {
        ValidationUtil.requireNonNull(response, "response", "ApiClientUtil.validateResponseStatus");
        if (response.getStatusCode() != expectedStatus) {
            throw new AssertionError(String.format("Expected status code %d but got %d", expectedStatus, response.getStatusCode()));
        }
    }

    // ── Parameter Validation Delegates ───────────────────────────────────────

    /** Delegates to {@link ValidationUtil#requireNonNull(Object, String)}. */
    public static <T> T requireNonNull(T param, String paramName) {
        return ValidationUtil.requireNonNull(param, paramName);
    }

    /** Delegates to {@link ValidationUtil#requireNonNull(Object, String, String)}. */
    public static <T> T requireNonNull(T param, String paramName, String context) {
        return ValidationUtil.requireNonNull(param, paramName, context);
    }

    /** Delegates to {@link ValidationUtil#requireNonEmpty(String, String)}. */
    public static String requireNonEmpty(String param, String paramName) {
        return ValidationUtil.requireNonEmpty(param, paramName);
    }

    /** Delegates to {@link ValidationUtil#requireNonEmpty(String, String, String)}. */
    public static String requireNonEmpty(String param, String paramName, String context) {
        return ValidationUtil.requireNonEmpty(param, paramName, context);
    }

    /** Delegates to {@link ValidationUtil#requireNonEmpty(Collection, String)}. */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String paramName) {
        return ValidationUtil.requireNonEmpty(collection, paramName);
    }

    /** Delegates to {@link ValidationUtil#requireNonEmpty(Map, String)}. */
    public static <K, V> Map<K, V> requireNonEmpty(Map<K, V> map, String paramName) {
        return ValidationUtil.requireNonEmpty(map, paramName);
    }

    /** Delegates to {@link ValidationUtil#requireNonEmpty(byte[], String)}. */
    public static byte[] requireNonEmpty(byte[] array, String paramName) {
        return ValidationUtil.requireNonEmpty(array, paramName);
    }
}
