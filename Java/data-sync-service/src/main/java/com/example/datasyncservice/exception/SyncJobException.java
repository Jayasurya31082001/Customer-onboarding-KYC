package com.example.datasyncservice.exception;

/**
 * General sync job exception wrapping unexpected failures during job orchestration.
 */
public class SyncJobException extends RuntimeException {

    public SyncJobException(String message) {
        super(message);
    }

    public SyncJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
