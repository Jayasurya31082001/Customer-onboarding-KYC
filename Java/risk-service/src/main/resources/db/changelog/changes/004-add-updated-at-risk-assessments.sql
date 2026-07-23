-- Changeset: 004-add-updated-at-risk-assessments
-- Adds updated_at column for incremental data sync support.
-- Back-fills existing rows with assessed_at value.

ALTER TABLE risk_assessments ADD COLUMN updated_at TIMESTAMP NULL;

UPDATE risk_assessments SET updated_at = assessed_at WHERE updated_at IS NULL;

CREATE INDEX idx_risk_assessments_updated_at ON risk_assessments(updated_at);
