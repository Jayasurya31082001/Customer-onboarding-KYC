package com.example.datasyncservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for handling sync timestamps.
 *
 * <p>The {@code lastSyncTime} parameter sent to upstream services must be
 * slightly in the past (with a configurable overlap window) to account for
 * clock skew between microservices and avoid missing edge-case records.
 */
public final class SyncTimeUtil {

    /** ISO-8601 formatter accepted by all upstream /internal/sync endpoints. */
    public static final DateTimeFormatter SYNC_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * How far back we look relative to the stored lastSuccessfulSync timestamp.
     * A 60-second overlap ensures that records written within the last window
     * of a previous cycle are not missed due to clock skew.
     */
    public static final long OVERLAP_SECONDS = 60L;

    private SyncTimeUtil() { /* utility class */ }

    /**
     * Returns the effective sync start time — the stored cursor minus the overlap window.
     *
     * @param lastSuccessfulSync the raw timestamp stored in sync_metadata
     * @return the adjusted timestamp to use as the {@code lastSyncTime} query parameter
     */
    public static LocalDateTime effectiveSyncFrom(LocalDateTime lastSuccessfulSync) {
        if (lastSuccessfulSync == null) {
            // First ever run — sync everything from the epoch start
            return LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        return lastSuccessfulSync.minusSeconds(OVERLAP_SECONDS);
    }

    /**
     * Formats a {@link LocalDateTime} into the ISO-8601 string expected
     * by all upstream {@code /internal/sync} query parameters.
     *
     * @param dateTime the timestamp to format
     * @return ISO-8601 string, e.g. "2026-07-23T08:00:00"
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(SYNC_FORMATTER);
    }
}
