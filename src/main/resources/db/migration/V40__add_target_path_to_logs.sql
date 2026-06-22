ALTER TABLE audit_logs
    ADD COLUMN target_path VARCHAR(500) NULL AFTER target_id;

ALTER TABLE activity_logs
    ADD COLUMN target_path VARCHAR(500) NULL AFTER target_id;
