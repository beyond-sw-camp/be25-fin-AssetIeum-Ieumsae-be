ALTER TABLE purchase_policies
    ADD COLUMN purchase_method VARCHAR(30) NOT NULL DEFAULT 'PARALLEL' COMMENT '구매 방식'
        AFTER company_id;

UPDATE purchase_policies
SET purchase_method = CASE
    WHEN is_exclusive_asset_team = 1 AND allow_direct_purchase = 0 THEN 'ONLY_ASSET_TEAM'
    WHEN is_exclusive_asset_team = 0 AND allow_direct_purchase = 1 THEN 'ONLY_DIRECT_PURCHASE'
    ELSE 'PARALLEL'
END;

ALTER TABLE purchase_policies
    DROP COLUMN is_exclusive_asset_team,
    DROP COLUMN allow_direct_purchase,
    DROP COLUMN allow_parallel_operation;

ALTER TABLE purchase_policies
    MODIFY COLUMN over_percentage_limit DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '초과 허용 범위 - 실제 결제 금액 초과 허용 범위 % 단위, 예: 10.00 = 10% 초과 허용';

ALTER TABLE purchase_policies
    ADD CONSTRAINT CK_purchase_policies_purchase_method
        CHECK (purchase_method IN (
            'ONLY_ASSET_TEAM',
            'ONLY_DIRECT_PURCHASE',
            'PARALLEL'
        ));
