package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.RiskSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RiskDataTransformer implements DataTransformer<RiskSyncRecord> {

    private static final String SOURCE_SERVICE = "risk-service";
    private static final String RECORD_TYPE    = "RISK";

    @Override
    public DatabricksRecord transform(RiskSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("risk_id",       record.riskId());
        payload.put("customer_id",   record.customerId());
        payload.put("risk_score",    record.riskScore());
        payload.put("risk_category", record.riskCategory());
        payload.put("decision",      record.decision());
        payload.put("assessed_at",   record.assessedAt() != null ? record.assessedAt().toString() : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.riskId() + record.decision());

        return DatabricksRecord.of(
                SOURCE_SERVICE, RECORD_TYPE, record.riskId(),
                record.customerId(), payload, record.assessedAt(), checksum);
    }

    @Override
    public String supportedType() {
        return RiskSyncRecord.class.getSimpleName();
    }
}
