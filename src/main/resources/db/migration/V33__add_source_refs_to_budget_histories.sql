ALTER TABLE `budget_histories`
    ADD COLUMN IF NOT EXISTS `ticket_id` CHAR(36) NULL COMMENT 'Related ticket ID'
        AFTER `budget_id`,
    ADD COLUMN IF NOT EXISTS `purchase_plan_id` CHAR(36) NULL COMMENT 'Related purchase plan ID'
        AFTER `ticket_id`;

CREATE INDEX IF NOT EXISTS `IX_budget_histories_ticket_id`
    ON `budget_histories` (`ticket_id`);

CREATE INDEX IF NOT EXISTS `IX_budget_histories_purchase_plan_id`
    ON `budget_histories` (`purchase_plan_id`);

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'budget_histories'
        AND CONSTRAINT_NAME = 'FK_budget_histories_ticket_id_tickets'
);

SET @sql := IF(
    @constraint_exists = 0,
    'ALTER TABLE `budget_histories`
        ADD CONSTRAINT `FK_budget_histories_ticket_id_tickets`
        FOREIGN KEY (`ticket_id`)
        REFERENCES `tickets` (`ticket_id`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'budget_histories'
        AND CONSTRAINT_NAME = 'FK_budget_histories_purchase_plan_id_purchase_plans'
);

SET @sql := IF(
    @constraint_exists = 0,
    'ALTER TABLE `budget_histories`
        ADD CONSTRAINT `FK_budget_histories_purchase_plan_id_purchase_plans`
        FOREIGN KEY (`purchase_plan_id`)
        REFERENCES `purchase_plans` (`plan_id`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
