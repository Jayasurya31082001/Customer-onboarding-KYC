package com.example.datasyncservice.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility for generating SHA-256 checksums used in idempotency checks.
 *
 * <p>The {@code DatabricksRecord.checksum} field is computed here and used
 * as the MERGE key in Databricks Delta — re-sending the same record is a no-op.
 */
public final class ChecksumUtil {

    private ChecksumUtil() { /* utility class */ }

    /**
     * Computes a SHA-256 hex digest of the given input string.
     *
     * @param input the string to hash (must not be null)
     * @return lowercase hex-encoded SHA-256 digest
     */
    public static String sha256(String input) {
        if (input == null) {
            return sha256("null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JVM spec — this is unreachable
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
