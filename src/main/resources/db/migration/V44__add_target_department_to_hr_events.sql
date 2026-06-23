ALTER TABLE `hr_events`
    ADD COLUMN `target_department_id` CHAR(36) NULL AFTER `department_id`;

ALTER TABLE `hr_events`
    ADD CONSTRAINT `FK_hr_events_target_department`
        FOREIGN KEY (`target_department_id`) REFERENCES `departments` (`department_id`);

CREATE INDEX `IDX_hr_events_target_department`
    ON `hr_events` (`target_department_id`);
