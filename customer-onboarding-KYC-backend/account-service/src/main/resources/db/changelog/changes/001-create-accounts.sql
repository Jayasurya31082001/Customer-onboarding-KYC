CREATE TABLE accounts (
    account_id VARCHAR(36) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    account_number VARCHAR(8) NOT NULL UNIQUE,
    sort_code VARCHAR(8) NOT NULL,
    account_type VARCHAR(20) NOT NULL DEFAULT 'CURRENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_customer_id ON accounts(customer_id);
