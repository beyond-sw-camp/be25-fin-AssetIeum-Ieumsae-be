ALTER TABLE `purchase_request_tickets`
	ADD COLUMN IF NOT EXISTS `is_standard` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Standard asset item flag' AFTER `requested_usage_type`,
	ADD COLUMN IF NOT EXISTS `tangible_asset_item_id` CHAR(36) NULL COMMENT 'Standard tangible asset item ID' AFTER `is_standard`,
	ADD COLUMN IF NOT EXISTS `intangible_asset_item_id` CHAR(36) NULL COMMENT 'Standard intangible asset item ID' AFTER `tangible_asset_item_id`;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_request_tickets'
		AND CONSTRAINT_NAME = 'CK_purchase_request_tickets_item_xor'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_request_tickets`
		ADD CONSTRAINT `CK_purchase_request_tickets_item_xor`
		CHECK (((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL)) <= 1)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_request_tickets'
		AND CONSTRAINT_NAME = 'CK_purchase_request_tickets_standard_item_xor'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_request_tickets`
		ADD CONSTRAINT `CK_purchase_request_tickets_standard_item_xor`
		CHECK (
			(`is_standard` = 1 AND ((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL)) = 1)
			OR (`is_standard` = 0 AND `tangible_asset_item_id` IS NULL AND `intangible_asset_item_id` IS NULL)
		)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_request_tickets'
		AND CONSTRAINT_NAME = 'CK_purchase_request_tickets_item_category_match'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_request_tickets`
		ADD CONSTRAINT `CK_purchase_request_tickets_item_category_match`
		CHECK (
			(`tangible_asset_item_id` IS NULL OR `tangible_asset_category_id` IS NOT NULL)
			AND (`intangible_asset_item_id` IS NULL OR `intangible_asset_category_id` IS NOT NULL)
		)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_request_tickets'
		AND CONSTRAINT_NAME = 'FK_purchase_request_tickets_tangible_item_id'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_request_tickets`
		ADD CONSTRAINT `FK_purchase_request_tickets_tangible_item_id`
		FOREIGN KEY (`tangible_asset_item_id`)
		REFERENCES `tangible_asset_items` (`tangible_asset_item_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_request_tickets'
		AND CONSTRAINT_NAME = 'FK_purchase_request_tickets_intangible_item_id'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_request_tickets`
		ADD CONSTRAINT `FK_purchase_request_tickets_intangible_item_id`
		FOREIGN KEY (`intangible_asset_item_id`)
		REFERENCES `intangible_asset_items` (`intangible_asset_item_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
