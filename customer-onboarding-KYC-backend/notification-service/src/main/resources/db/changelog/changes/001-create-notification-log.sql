CREATE TABLE notification_log (
    notification_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    template_key VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_notification_log_customer_id ON notification_log (customer_id);
