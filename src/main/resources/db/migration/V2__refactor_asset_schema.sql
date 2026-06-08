-- tangible_asset_items
ALTER TABLE `tangible_asset_items`
    CHANGE COLUMN `item_code` `product_name` VARCHAR(255) NOT NULL
        AFTER `category_id`;

-- intangible_asset_items
ALTER TABLE `intangible_asset_items`
    CHANGE COLUMN `item_code` `product_name` VARCHAR(255) NOT NULL
        AFTER `category_id`;

ALTER TABLE `intangible_asset_items`
    ADD COLUMN `license_type` VARCHAR(30) NULL
        AFTER `provider`;

UPDATE `intangible_asset_items`
SET `license_type` = 'SUBSCRIPTION'
WHERE `license_type` IS NULL;

ALTER TABLE `intangible_asset_items`
    MODIFY COLUMN `license_type` VARCHAR(30) NOT NULL;

ALTER TABLE `intangible_asset_items`
    DROP COLUMN `name`;

-- intangible_assets
ALTER TABLE `intangible_assets`
    DROP COLUMN `license_type`;

-- purchase_requests -> purchase_plans
RENAME TABLE `purchase_requests`
    TO `purchase_plans`;

ALTER TABLE `purchase_plans`
    CHANGE COLUMN `request_id` `plan_id` CHAR(36) NOT NULL FIRST;

ALTER TABLE `purchase_plans`
    ADD COLUMN `plan_no` VARCHAR(100) NULL
        AFTER `requester_id`;

UPDATE `purchase_plans`
SET `plan_no` = CONCAT('PLAN-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', `plan_id`)
WHERE `plan_no` IS NULL;

ALTER TABLE `purchase_plans`
    MODIFY COLUMN `plan_no` VARCHAR(100) NOT NULL
        AFTER `requester_id`;

ALTER TABLE `purchase_plans`
    MODIFY COLUMN `approved_at` DATETIME NULL
        AFTER `delivery_date`;

-- purchase_request_items -> purchase_plan_items
RENAME TABLE `purchase_request_items`
    TO `purchase_plan_items`;

ALTER TABLE `purchase_plan_items`
    CHANGE COLUMN `request_id` `plan_id` CHAR(36) NOT NULL
        AFTER `company_id`;

-- purchase_evidences
ALTER TABLE `purchase_evidences`
    CHANGE COLUMN `request_id` `plan_id` CHAR(36) NOT NULL
        AFTER `company_id`;

-- hr_template_items
ALTER TABLE `hr_template_items`
    MODIFY COLUMN `quantity` INT NULL
        AFTER `tangible_asset_item_id`;