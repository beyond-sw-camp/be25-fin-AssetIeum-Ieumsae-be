ALTER TABLE `asset_request_tickets`
	DROP CONSTRAINT IF EXISTS `CK_asset_request_tickets_XOR_4`;

ALTER TABLE `asset_request_tickets`
	DROP FOREIGN KEY IF EXISTS `FK_asset_request_tickets_tangible_asset_id_tangible_assets`;

ALTER TABLE `asset_request_tickets`
	DROP FOREIGN KEY IF EXISTS `FK_asset_request_tickets_intangible_asset_id_intangible_asse`;

ALTER TABLE `asset_request_tickets`
	DROP COLUMN IF EXISTS `tangible_asset_id`,
	DROP COLUMN IF EXISTS `intangible_asset_id`;

ALTER TABLE `asset_request_tickets`
	ADD COLUMN IF NOT EXISTS `quantity` INT NOT NULL DEFAULT 1
	COMMENT 'Request quantity'
	AFTER `requested_usage_type`;

ALTER TABLE `asset_request_tickets`
	ADD COLUMN IF NOT EXISTS `tangible_asset_item_id` CHAR(36) NULL
	COMMENT 'Tangible asset item ID'
	AFTER `request_method`,
	ADD COLUMN IF NOT EXISTS `intangible_asset_item_id` CHAR(36) NULL
	COMMENT 'Intangible asset item ID'
	AFTER `tangible_asset_item_id`,
	ADD COLUMN IF NOT EXISTS `requested_item_name` VARCHAR(100) NULL
	COMMENT 'Requested item name'
	AFTER `intangible_asset_item_id`,
	ADD COLUMN IF NOT EXISTS `expected_price` DECIMAL(15,2) NULL
	COMMENT 'Expected price'
	AFTER `requested_item_name`,
	ADD COLUMN IF NOT EXISTS `actual_price` DECIMAL(15,2) NULL
	COMMENT 'Actual price'
	AFTER `expected_price`;

ALTER TABLE `asset_request_tickets`
	ADD CONSTRAINT IF NOT EXISTS `CK_asset_request_tickets_quantity`
	CHECK (`quantity` >= 1);
