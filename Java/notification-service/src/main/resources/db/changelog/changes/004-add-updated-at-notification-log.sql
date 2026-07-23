-- Changeset: 004-add-updated-at-notification-log
-- Adds updated_at column for incremental data sync support.
-- Back-fills existing rows with created_at value.

ALTER TABLE notification_log ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE notification_log SET updated_at = created_at WHERE updated_at IS NULL;

CREATE INDEX idx_notification_log_updated_at ON notification_log(updated_at);
