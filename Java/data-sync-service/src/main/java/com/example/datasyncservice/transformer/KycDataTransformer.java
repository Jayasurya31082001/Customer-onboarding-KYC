package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.KycSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KycDataTransformer implements DataTransformer<KycSyncRecord> {

    private static final String SOURCE_SERVICE = "kyc-service";
    private static final String RECORD_TYPE    = "KYC";

    @Override
    public DatabricksRecord transform(KycSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("kyc_id",          record.kycId());
        payload.put("customer_id",     record.customerId());
        payload.put("overall_status",  record.overallStatus());
        payload.put("risk_level",      record.riskLevel());
        payload.put("completed_at",    record.completedAt() != null ? record.completedAt().toString() : null);
        payload.put("updated_at",      record.updatedAt()  != null ? record.updatedAt().toString()  : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.kycId() + record.overallStatus());

        return DatabricksRecord.of(
                SOURCE_SERVICE, RECORD_TYPE, record.kycId(),
                record.customerId(), payload,
                record.updatedAt() != null ? record.updatedAt() : record.completedAt(),
                checksum);
    }

    @Override
    public String supportedType() {
        return KycSyncRecord.class.getSimpleName();
    }
}
