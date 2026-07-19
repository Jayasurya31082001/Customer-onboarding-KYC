package com.example.documentservice.document.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.documentservice.kyc.client.KycEventPublisherClient;

@ExtendWith(MockitoExtension.class)
class DocumentUploadedEventForwarderTest {

    @Mock
    private KycEventPublisherClient publisherClient;

    @Test
    void onDocumentUploaded_forwardsToKycClient() {
        DocumentUploadedEventForwarder forwarder = new DocumentUploadedEventForwarder(publisherClient);
        DocumentUploadedEvent event = new DocumentUploadedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "passport.pdf",
                "application/pdf",
                LocalDateTime.now(),
                "corr-1");

        forwarder.onDocumentUploaded(event);

        verify(publisherClient).publishDocumentUploaded(any());
    }

    @Test
    void recover_doesNotThrow() {
        DocumentUploadedEventForwarder forwarder = new DocumentUploadedEventForwarder(publisherClient);
        DocumentUploadedEvent event = new DocumentUploadedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "passport.pdf",
                "application/pdf",
                LocalDateTime.now(),
                "corr-1");

        forwarder.recover(new RuntimeException("failure"), event);
    }
}
