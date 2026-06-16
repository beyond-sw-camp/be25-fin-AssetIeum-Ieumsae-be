ALTER TABLE purchase_plans
    ADD COLUMN deleted_at DATETIME NULL DEFAULT NULL COMMENT '삭제 일시'
        AFTER updated_at;
