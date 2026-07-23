package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.NotificationSyncRecord;
import com.example.datasyncservice.util.ChecksumUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationDataTransformer implements DataTransformer<NotificationSyncRecord> {

    private static final String SOURCE_SERVICE = "notification-service";
    private static final String RECORD_TYPE    = "NOTIFICATION";

    @Override
    public DatabricksRecord transform(NotificationSyncRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notification_id", record.notificationId());
        payload.put("customer_id",     record.customerId());
        payload.put("channel",         record.channel());
        payload.put("status",          record.status());
        payload.put("event_type",      record.eventType());
        payload.put("sent_at",         record.sentAt() != null ? record.sentAt().toString() : null);

        String checksum = ChecksumUtil.sha256(RECORD_TYPE + record.notificationId() + record.status());

        return DatabricksRecord.of(
                SOURCE_SERVICE, RECORD_TYPE, record.notificationId(),
                record.customerId(), payload, record.sentAt(), checksum);
    }

    @Override
    public String supportedType() {
        return NotificationSyncRecord.class.getSimpleName();
    }
}
