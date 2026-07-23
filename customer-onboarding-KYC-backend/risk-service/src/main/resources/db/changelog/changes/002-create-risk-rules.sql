CREATE TABLE risk_rules (
    rule_id VARCHAR(36) NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    risk_points INT NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT pk_risk_rules PRIMARY KEY (rule_id)
);