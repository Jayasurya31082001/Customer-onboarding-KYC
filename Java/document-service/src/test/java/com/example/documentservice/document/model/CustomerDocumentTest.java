package com.example.documentservice.document.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerDocumentTest {

    @Test
    void prePersist_setsGeneratedFieldsWhenMissing() {
        CustomerDocument document = new CustomerDocument();
        document.setCustomerId(UUID.randomUUID());
        document.setFileName("passport.pdf");
        document.setContentType("application/pdf");
        document.setSizeInBytes(10L);
        document.setContent(new byte[]{1, 2, 3});

        document.prePersist();

        assertThat(document.getDocumentId()).isNotNull();
        assertThat(document.getCreatedAt()).isNotNull();
    }

    @Test
    void gettersAndSetters_coverAllFields() {
        CustomerDocument document = new CustomerDocument();
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        document.setDocumentId(id);
        document.setCustomerId(customerId);
        document.setFileName("passport.pdf");
        document.setContentType("application/pdf");
        document.setSizeInBytes(2048L);
        document.setContent(new byte[]{1, 2});
        document.setCreatedAt(createdAt);

        assertThat(document.getDocumentId()).isEqualTo(id);
        assertThat(document.getCustomerId()).isEqualTo(customerId);
        assertThat(document.getFileName()).isEqualTo("passport.pdf");
        assertThat(document.getContentType()).isEqualTo("application/pdf");
        assertThat(document.getSizeInBytes()).isEqualTo(2048L);
        assertThat(document.getContent()).containsExactly((byte) 1, (byte) 2);
        assertThat(document.getCreatedAt()).isEqualTo(createdAt);
    }
}
