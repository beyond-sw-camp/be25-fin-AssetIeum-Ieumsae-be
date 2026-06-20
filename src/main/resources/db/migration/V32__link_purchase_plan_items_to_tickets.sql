SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'purchase_plan_items'
        AND CONSTRAINT_NAME = 'FK_purchase_plan_items_ticket_id_purchase_request_tickets'
);

SET @sql := IF(
    @constraint_exists = 1,
    'ALTER TABLE `purchase_plan_items`
        DROP FOREIGN KEY `FK_purchase_plan_items_ticket_id_purchase_request_tickets`',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'purchase_plan_items'
        AND CONSTRAINT_NAME = 'FK_purchase_plan_items_ticket_id_tickets'
);

SET @sql := IF(
    @constraint_exists = 0,
    'ALTER TABLE `purchase_plan_items`
        ADD CONSTRAINT `FK_purchase_plan_items_ticket_id_tickets`
        FOREIGN KEY (`ticket_id`)
        REFERENCES `tickets` (`ticket_id`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
