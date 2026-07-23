CREATE TABLE risk_assessments (
    assessment_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    score INT NOT NULL,
    disposition VARCHAR(32) NOT NULL,
    assessed_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_risk_assessments PRIMARY KEY (assessment_id)
);

CREATE INDEX idx_risk_assessments_customer_id ON risk_assessments (customer_id);