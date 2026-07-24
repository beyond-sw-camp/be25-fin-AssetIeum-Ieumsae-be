ALTER TABLE notifications
    ADD COLUMN event_id CHAR(36) NULL AFTER notification_id,
    ADD CONSTRAINT uk_notifications_event_id UNIQUE (event_id);
