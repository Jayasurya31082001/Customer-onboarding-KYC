package com.example.datasyncservice.exception;

/**
 * Thrown when the Databricks write fails after all retry attempts are exhausted.
 *
 * <p>This is a non-retryable terminal exception at the Step level — it causes
 * the Spring Batch Step to transition to FAILED status, which triggers
 * {@link com.example.datasyncservice.batch.listener.SyncStepExecutionListener}
 * to persist the FAILED state in sync_metadata.
 *
 * <p>The sync cursor (lastSuccessfulSync) is NOT advanced on this failure,
 * so the next scheduled run will retry from the same point.
 */
public class DatabricksWriteException extends RuntimeException {

    public DatabricksWriteException(String message) {
        super(message);
    }

    public DatabricksWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
