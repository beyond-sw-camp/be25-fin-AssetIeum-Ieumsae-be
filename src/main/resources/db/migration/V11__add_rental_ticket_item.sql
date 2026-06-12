ALTER TABLE `rental_tickets`
	ADD COLUMN IF NOT EXISTS `tangible_asset_item_id` CHAR(36) NOT NULL
	COMMENT 'Requested tangible asset item ID'
	AFTER `tangible_asset_id`;

SET @fk_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'rental_tickets'
		AND CONSTRAINT_NAME = 'FK_rental_tickets_tangible_asset_item_id_tangible_asset_items'
);

SET @sql := IF(@fk_exists = 0,
	'ALTER TABLE `rental_tickets`
		ADD CONSTRAINT `FK_rental_tickets_tangible_asset_item_id_tangible_asset_items`
		FOREIGN KEY (`tangible_asset_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `asset_request_tickets`
	DROP CONSTRAINT IF EXISTS `CK_asset_request_tickets_XOR_3`;

ALTER TABLE `asset_request_tickets`
	DROP COLUMN IF EXISTS `requested_item_name`;

ALTER TABLE `asset_request_tickets`
	ADD CONSTRAINT IF NOT EXISTS `CK_asset_request_tickets_item_xor`
	CHECK (((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL)) = 1);
