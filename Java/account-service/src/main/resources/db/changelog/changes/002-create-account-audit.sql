CREATE TABLE account_audit (
    audit_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    details VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_account_audit_account_id ON account_audit(account_id);
