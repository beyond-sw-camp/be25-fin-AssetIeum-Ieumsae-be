ALTER TABLE purchase_plans
    MODIFY COLUMN ordered_at DATETIME NULL COMMENT '발주 일시';

ALTER TABLE purchase_plans
    ADD COLUMN item_count INT NOT NULL DEFAULT 0 COMMENT '품목 수'
        AFTER actual_amount;

UPDATE purchase_plans pp
SET item_count = (
    SELECT COUNT(*)
    FROM purchase_plan_items ppi
    WHERE ppi.plan_id = pp.plan_id
);
