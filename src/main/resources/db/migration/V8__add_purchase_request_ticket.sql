SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'asset_request_tickets'
		AND CONSTRAINT_NAME = 'FK_asset_request_tickets_tangible_asset_item_id_tangible_ass'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `asset_request_tickets`
		ADD CONSTRAINT `FK_asset_request_tickets_tangible_asset_item_id_tangible_ass`
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
		AND TABLE_NAME = 'asset_request_tickets'
		AND CONSTRAINT_NAME = 'FK_asset_request_tickets_intangible_asset_item_id_intangible'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `asset_request_tickets`
		ADD CONSTRAINT `FK_asset_request_tickets_intangible_asset_item_id_intangible`
		FOREIGN KEY (`intangible_asset_item_id`)
		REFERENCES `intangible_asset_items` (`intangible_asset_item_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `asset_request_tickets`
	DROP CONSTRAINT IF EXISTS `CK_asset_request_tickets_request_method_2`;

ALTER TABLE `asset_request_tickets`
	DROP COLUMN IF EXISTS `request_method`;

CREATE TABLE IF NOT EXISTS `purchase_request_tickets` (
	`ticket_id` CHAR(36) NOT NULL COMMENT 'Purchase request ticket ID',
	`company_id` CHAR(36) NOT NULL COMMENT 'Company ID',
	`purchase_request_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT 'Purchase request ticket status',
	`request_method` VARCHAR(50) NOT NULL COMMENT 'Request processing method',
	`requested_usage_type` VARCHAR(30) NOT NULL COMMENT 'Requested usage type',
	`tangible_asset_category_id` CHAR(36) NULL COMMENT 'Tangible asset category ID',
	`intangible_asset_category_id` CHAR(36) NULL COMMENT 'Intangible asset category ID',
	`requested_item_detail` VARCHAR(500) NOT NULL COMMENT 'Requested item name and specification',
	`manufacturer` VARCHAR(100) NULL COMMENT 'Manufacturer',
	`license_type` VARCHAR(50) NULL COMMENT 'License type for intangible asset',
	`purchase_url` VARCHAR(500) NULL COMMENT 'Purchase URL',
	`quantity` INT NOT NULL DEFAULT 1 COMMENT 'Request quantity',
	`expected_price` DECIMAL(15,2) NULL COMMENT 'Expected price',
	`deleted_at` DATETIME NULL DEFAULT NULL COMMENT 'Deleted at',
	CONSTRAINT `PK_purchase_request_tickets` PRIMARY KEY (`ticket_id`),
	CONSTRAINT `CK_purchase_request_tickets_status`
		CHECK (`purchase_request_ticket_status` IN ('REQUESTED', 'ORDERED', 'RECEIVED', 'COMPLETED', 'CANCELLED')),
	CONSTRAINT `CK_purchase_request_tickets_request_method`
		CHECK (`request_method` IN ('TEAM_PURCHASE', 'DIRECT_PURCHASE')),
	CONSTRAINT `CK_purchase_request_tickets_requested_usage_type`
		CHECK (`requested_usage_type` IN ('PERSONAL', 'DEPARTMENT')),
	CONSTRAINT `CK_purchase_request_tickets_category_xor`
		CHECK (((`tangible_asset_category_id` IS NOT NULL) + (`intangible_asset_category_id` IS NOT NULL)) = 1),
	CONSTRAINT `CK_purchase_request_tickets_license_type`
		CHECK (`license_type` IS NULL OR `license_type` IN ('SUBSCRIPTION', 'PERPETUAL', 'TERM')),
	CONSTRAINT `CK_purchase_request_tickets_license_type_target`
		CHECK (`license_type` IS NULL OR `intangible_asset_category_id` IS NOT NULL),
	CONSTRAINT `CK_purchase_request_tickets_quantity`
		CHECK (`quantity` >= 1),
	CONSTRAINT `CK_purchase_request_tickets_expected_price`
		CHECK (`expected_price` IS NULL OR `expected_price` >= 0),
	CONSTRAINT `FK_purchase_request_tickets_ticket_id_tickets`
		FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`),
	CONSTRAINT `FK_purchase_request_tickets_company_id_companies`
		FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`),
	CONSTRAINT `FK_purchase_request_tickets_tangible_category_id`
		FOREIGN KEY (`tangible_asset_category_id`) REFERENCES `tangible_asset_categories` (`tangible_asset_category_id`),
	CONSTRAINT `FK_purchase_request_tickets_intangible_category_id`
		FOREIGN KEY (`intangible_asset_category_id`) REFERENCES `intangible_asset_categories` (`intangible_asset_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
