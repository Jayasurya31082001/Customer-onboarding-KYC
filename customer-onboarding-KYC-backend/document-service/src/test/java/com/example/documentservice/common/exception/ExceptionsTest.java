package com.example.documentservice.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

    @Test
    void resourceNotFoundException_hasMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");
        assertThat(ex).hasMessage("missing");
    }

    @Test
    void invalidDocumentException_hasMessage() {
        InvalidDocumentException ex = new InvalidDocumentException("invalid");
        assertThat(ex).hasMessage("invalid");
    }

    @Test
    void documentStorageException_hasMessageAndCause() {
        RuntimeException cause = new RuntimeException("io");
        DocumentStorageException ex = new DocumentStorageException("store failed", cause);
        assertThat(ex).hasMessage("store failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
