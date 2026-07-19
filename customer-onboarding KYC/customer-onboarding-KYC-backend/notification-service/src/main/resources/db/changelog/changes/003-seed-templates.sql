INSERT INTO templates (template_key, subject, content, active) VALUES
('ACCOUNT_CREATED', 'Account Created', 'Dear customer ${customerId}, your account ${accountNumber} with sort code ${sortCode} has been created.', TRUE),
('APPLICATION_REJECTED', 'Application Rejected', 'Dear customer ${customerId}, your onboarding application was rejected. Reason: ${reason}.', TRUE);
