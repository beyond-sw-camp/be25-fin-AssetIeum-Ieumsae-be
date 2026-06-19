ALTER TABLE `purchase_plan_items`
    ADD COLUMN `purchase_plan_item_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        COMMENT 'Purchase plan item status - PENDING, RECEIVED, ASSET_REGISTERED'
        AFTER `external_url`;

UPDATE `purchase_plan_items`
SET `purchase_plan_item_status` = 'RECEIVED'
WHERE `received_at` IS NOT NULL;

ALTER TABLE `purchase_plan_items`
    ADD CONSTRAINT `CK_purchase_plan_items_status`
        CHECK (`purchase_plan_item_status` IN ('PENDING', 'RECEIVED', 'ASSET_REGISTERED'));
