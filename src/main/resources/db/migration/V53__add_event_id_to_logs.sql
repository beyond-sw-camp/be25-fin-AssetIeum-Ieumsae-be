ALTER TABLE activity_logs
    ADD COLUMN event_id CHAR(36) NULL AFTER activity_log_id,
    ADD CONSTRAINT UK_activity_logs_event_id UNIQUE (event_id);

ALTER TABLE audit_logs
    ADD COLUMN event_id CHAR(36) NULL AFTER audit_log_id,
    ADD CONSTRAINT UK_audit_logs_event_id UNIQUE (event_id);
