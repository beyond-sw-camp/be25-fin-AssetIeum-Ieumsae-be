ALTER TABLE `purchase_plan_items`
    ADD COLUMN `asset_type` VARCHAR(20) NULL AFTER `ticket_id`;

ALTER TABLE `purchase_plan_items`
    CHANGE COLUMN `item_name` `product_name` VARCHAR(255) NOT NULL;

ALTER TABLE `purchase_plan_items`
    DROP CONSTRAINT `CK_purchase_request_items_XOR_1`;

ALTER TABLE `purchase_plan_items`
    ADD CONSTRAINT `CK_purchase_plan_items_asset_type`
        CHECK (`asset_type` IS NULL OR `asset_type` IN ('TANGIBLE', 'INTANGIBLE'));
