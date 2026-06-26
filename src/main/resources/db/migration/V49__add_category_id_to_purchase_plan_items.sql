ALTER TABLE `purchase_plan_items`
    ADD COLUMN `category_id` CHAR(36) NULL COMMENT '구매 계획 품목 카테고리 ID'
        AFTER `is_standard`;