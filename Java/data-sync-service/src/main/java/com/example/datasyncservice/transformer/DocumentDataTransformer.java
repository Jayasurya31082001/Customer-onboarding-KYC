package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.DocumentSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DocumentDataTransformer implements DataTransformer<DocumentSyncRecord> {

    private static final String SOURCE_SERVICE = "document-service";
    private static final String RECORD_TYPE    = "DOCUMENT";

    @Override
    public DatabricksRecord transform(DocumentSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("document_id",   record.documentId());
        payload.put("customer_id",   record.customerId());
        payload.put("document_type", record.documentType());
        payload.put("status",        record.status());
        payload.put("uploaded_at",   record.uploadedAt()  != null ? record.uploadedAt().toString()  : null);
        payload.put("verified_at",   record.verifiedAt() != null ? record.verifiedAt().toString() : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.documentId() + record.status());

        return DatabricksRecord.of(
                SOURCE_SERVICE, RECORD_TYPE, record.documentId(),
                record.customerId(), payload,
                record.verifiedAt() != null ? record.verifiedAt() : record.uploadedAt(),
                checksum);
    }

    @Override
    public String supportedType() {
        return DocumentSyncRecord.class.getSimpleName();
    }
}
