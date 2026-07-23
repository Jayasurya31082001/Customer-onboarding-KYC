package com.example.datasyncservice.dto.sync;

import java.util.List;

/**
 * Generic paginated response wrapper returned by all business microservice
 * {@code /internal/sync} endpoints.
 *
 * <p>Mirrors the standard Spring Data {@code Page} shape so that business
 * services can return a Spring {@code Page} serialised as JSON with minimal
 * extra code.
 *
 * @param <T> the type of sync record (CustomerSyncRecord, AccountSyncRecord, etc.)
 */
public record PagedSyncResponse<T>(

        /** The records for the current page. */
        List<T> content,

        /** 0-based page index that was requested. */
        int page,

        /** The requested page size. */
        int size,

        /** Total number of matching records across all pages. */
        long totalElements,

        /** Total number of pages available. */
        int totalPages,

        /** {@code true} if this is the last page. */
        boolean last
) {
    /** Convenience factory for an empty response (no records found). */
    public static <T> PagedSyncResponse<T> empty(int page, int size) {
        return new PagedSyncResponse<>(List.of(), page, size, 0, 0, true);
    }

    /** Returns {@code true} if the content list is empty. */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
