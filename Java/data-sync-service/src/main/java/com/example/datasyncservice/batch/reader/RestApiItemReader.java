package com.example.datasyncservice.batch.reader;

import com.example.datasyncservice.repository.SyncMetadataRepository;
import com.example.datasyncservice.util.SyncTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Generic paginated {@link ItemReader} that pulls incremental records from any
 * upstream microservice's {@code /internal/sync} endpoint.
 *
 * <p>This is the <b>single</b> reader implementation shared by all 6 steps.
 * Each step provides a different {@code fetchPage} function that calls the
 * appropriate service client.
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>On first {@link #read()} call, loads the last successful sync time from {@code sync_metadata}.</li>
 *   <li>Fetches page 0 from the upstream service, buffers all records in memory.</li>
 *   <li>Subsequent {@link #read()} calls drain the buffer one record at a time (Spring Batch pattern).</li>
 *   <li>When the buffer is empty, fetches the next page. Returns {@code null} when the last page is reached,
 *       signalling Spring Batch to move to the writer for the final chunk.</li>
 * </ol>
 *
 * <p><b>Thread-safety:</b> Spring Batch creates one reader instance per step execution
 * (StepScope), so no concurrent access occurs.
 *
 * @param <T> the sync record type returned by the upstream service
 */
public class RestApiItemReader<T> implements ItemReader<T> {

    private static final Logger log = LoggerFactory.getLogger(RestApiItemReader.class);

    /** Function(lastSyncTime, page) → list of records for that page. */
    private final BiFunction<LocalDateTime, Integer, List<T>> fetchPage;
    private final SyncMetadataRepository metadataRepo;
    private final String serviceName;
    private final int pageSize;

    // ── Mutable state (safe: StepScope per execution) ─────────────────────────
    private LocalDateTime lastSyncTime;
    private int currentPage = 0;
    private boolean lastPageReached = false;
    private final List<T> buffer = new ArrayList<>();

    public RestApiItemReader(
            BiFunction<LocalDateTime, Integer, List<T>> fetchPage,
            SyncMetadataRepository metadataRepo,
            String serviceName,
            int pageSize) {
        this.fetchPage    = fetchPage;
        this.metadataRepo = metadataRepo;
        this.serviceName  = serviceName;
        this.pageSize     = pageSize;
    }

    @Override
    public T read() {
        // Initialise cursor on first call
        if (lastSyncTime == null) {
            lastSyncTime = resolveLastSyncTime();
            log.info("[{}] Starting incremental read from: {}", serviceName, lastSyncTime);
        }

        // Drain buffer first
        if (!buffer.isEmpty()) {
            return buffer.remove(0);
        }

        // No more pages → signal end to Spring Batch
        if (lastPageReached) {
            log.info("[{}] All pages consumed. Total pages fetched: {}", serviceName, currentPage);
            return null;
        }

        // Fetch next page
        List<T> page = fetchPage.apply(lastSyncTime, currentPage);

        if (page == null || page.isEmpty()) {
            lastPageReached = true;
            log.info("[{}] Empty page received at page={}. Sync complete.", serviceName, currentPage);
            return null;
        }

        log.debug("[{}] Fetched {} records from page={}", serviceName, page.size(), currentPage);
        buffer.addAll(page);
        currentPage++;

        // If the page is smaller than the page size, it's the last page
        if (page.size() < pageSize) {
            lastPageReached = true;
        }

        return buffer.isEmpty() ? null : buffer.remove(0);
    }

    private LocalDateTime resolveLastSyncTime() {
        return metadataRepo.findByServiceName(serviceName)
                .map(meta -> SyncTimeUtil.effectiveSyncFrom(meta.getLastSuccessfulSync()))
                .orElseGet(() -> SyncTimeUtil.effectiveSyncFrom(null));
    }
}
