package com.example.documentservice.document.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.documentservice.kyc.client.DocumentUploadedIngressRequest;
import com.example.documentservice.kyc.client.KycEventPublisherClient;

@Component
public class DocumentUploadedEventForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUploadedEventForwarder.class);

    private final KycEventPublisherClient kycEventPublisherClient;

    public DocumentUploadedEventForwarder(KycEventPublisherClient kycEventPublisherClient) {
        this.kycEventPublisherClient = kycEventPublisherClient;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        kycEventPublisherClient.publishDocumentUploaded(new DocumentUploadedIngressRequest(
                event.documentId(),
                event.customerId(),
                event.occurredAt(),
                event.correlationId()
        ));

        LOGGER.info("Forwarded document-uploaded event to kyc-service. customerId={}, documentId={}, correlationId={}",
                event.customerId(), event.documentId(), event.correlationId());
    }

    @Recover
    public void recover(Exception exception, DocumentUploadedEvent event) {
        LOGGER.error("Failed to forward document-uploaded event after retries. customerId={}, documentId={}",
                event.customerId(), event.documentId(), exception);
    }
}
