package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.CustomerSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforms a {@link CustomerSyncRecord} into the canonical {@link DatabricksRecord}.
 *
 * <p>Responsibility: field mapping, null-safety, and checksum generation.
 * No Databricks SDK or HTTP logic here — pure data transformation.
 *
 * <p>Email is intentionally included in the payload in this example; in a
 * production PII-sensitive context, replace with a one-way hash before syncing.
 */
@Component
public class CustomerDataTransformer implements DataTransformer<CustomerSyncRecord> {

    private static final String SOURCE_SERVICE = "customer-service";
    private static final String RECORD_TYPE    = "CUSTOMER";

    @Override
    public DatabricksRecord transform(CustomerSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customer_id",   record.customerId());
        payload.put("first_name",    record.firstName());
        payload.put("last_name",     record.lastName());
        payload.put("email_hash",    ChecksumUtil.sha256(record.email())); // PII masking
        payload.put("date_of_birth", record.dateOfBirth() != null ? record.dateOfBirth().toString() : null);
        payload.put("nationality",   record.nationality());
        payload.put("city",          record.city());
        payload.put("postcode",      record.postcode());
        payload.put("status",        record.status());
        payload.put("created_at",    record.createdAt() != null ? record.createdAt().toString() : null);
        payload.put("updated_at",    record.updatedAt() != null ? record.updatedAt().toString() : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.customerId() + record.status());

        return DatabricksRecord.of(
                SOURCE_SERVICE,
                RECORD_TYPE,
                record.customerId(),
                record.customerId(),
                payload,
                record.updatedAt() != null ? record.updatedAt() : record.createdAt(),
                checksum
        );
    }

    @Override
    public String supportedType() {
        return CustomerSyncRecord.class.getSimpleName();
    }
}
