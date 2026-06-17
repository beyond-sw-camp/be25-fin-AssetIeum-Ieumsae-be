ALTER TABLE `hr_events`
    MODIFY COLUMN `event_date` DATETIME NOT NULL COMMENT '예정일';

ALTER TABLE `hr_events`
    ADD COLUMN `hr_event_no` VARCHAR(100) NOT NULL COMMENT 'HR 이벤트 번호'
        AFTER `member_id`;

ALTER TABLE `hr_events`
    MODIFY COLUMN `executed_at` DATETIME NULL COMMENT '실행일';

