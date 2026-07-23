-- Changeset: 003-add-updated-at-accounts
-- Adds updated_at column for incremental data sync support.
-- Back-fills existing rows with created_at value.

ALTER TABLE accounts ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE accounts SET updated_at = created_at WHERE updated_at IS NULL;

CREATE INDEX idx_accounts_updated_at ON accounts(updated_at);
