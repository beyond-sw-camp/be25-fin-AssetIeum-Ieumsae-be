ALTER TABLE `hr_events`
    DROP CONSTRAINT `CK_hr_events_event_type_1`;

ALTER TABLE `hr_events`
    ADD CONSTRAINT `CK_hr_events_event_type_1`
        CHECK (`event_type` IN ('ONBOARDING', 'OFFBOARDING', 'DEPARTMENT_TRANSFER'));
