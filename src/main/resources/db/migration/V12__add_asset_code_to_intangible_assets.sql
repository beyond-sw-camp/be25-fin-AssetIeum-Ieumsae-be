ALTER TABLE `intangible_assets`
    MODIFY COLUMN `license_code` VARCHAR(50) NULL COMMENT 'License code';

ALTER TABLE `intangible_assets`
    ADD COLUMN `asset_code` VARCHAR(50) NULL COMMENT 'Asset code' AFTER `intangible_item_id`;

UPDATE `intangible_assets`
SET `asset_code` = CONCAT('IA-', REPLACE(`intangible_asset_id`, '-', ''))
WHERE `asset_code` IS NULL;

ALTER TABLE `intangible_assets`
    MODIFY COLUMN `asset_code` VARCHAR(50) NOT NULL COMMENT 'Asset code';

ALTER TABLE `intangible_assets`
    ADD CONSTRAINT `UK_intangible_assets_asset_code` UNIQUE (`asset_code`);
