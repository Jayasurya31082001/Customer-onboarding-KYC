package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.AccountSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AccountDataTransformer implements DataTransformer<AccountSyncRecord> {

    private static final String SOURCE_SERVICE = "account-service";
    private static final String RECORD_TYPE    = "ACCOUNT";

    @Override
    public DatabricksRecord transform(AccountSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account_id",     record.accountId());
        payload.put("customer_id",    record.customerId());
        payload.put("account_number", record.accountNumber());
        payload.put("sort_code",      record.sortCode());
        payload.put("account_type",   record.accountType());
        payload.put("status",         record.status());
        payload.put("created_at",     record.createdAt() != null ? record.createdAt().toString() : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.accountId() + record.status());

        return DatabricksRecord.of(
                SOURCE_SERVICE, RECORD_TYPE, record.accountId(),
                record.customerId(), payload, record.createdAt(), checksum);
    }

    @Override
    public String supportedType() {
        return AccountSyncRecord.class.getSimpleName();
    }
}
