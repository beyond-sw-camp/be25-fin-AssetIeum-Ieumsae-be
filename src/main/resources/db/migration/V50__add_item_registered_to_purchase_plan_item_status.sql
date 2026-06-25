ALTER TABLE `purchase_plan_items`
    DROP CONSTRAINT `CK_purchase_plan_items_status`;

ALTER TABLE `purchase_plan_items`
    ADD CONSTRAINT `CK_purchase_plan_items_status`
        CHECK (`purchase_plan_item_status` IN ('PENDING', 'ITEM_REGISTERED', 'RECEIVED', 'ASSET_REGISTERED'));
