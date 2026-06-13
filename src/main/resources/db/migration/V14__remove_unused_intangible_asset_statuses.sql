UPDATE `intangible_assets`
SET `intangible_asset_status` = 'IN_USE'
WHERE `intangible_asset_status` IN ('EXPIRING_SOON', 'CANCEL_REQUESTED');

ALTER TABLE `intangible_assets`
    DROP CONSTRAINT `CK_intangible_assets_intangible_asset_status_2`;

ALTER TABLE `intangible_assets`
    ADD CONSTRAINT `CK_intangible_assets_intangible_asset_status_2`
        CHECK (`intangible_asset_status` IN ('AVAILABLE', 'IN_USE', 'EXPIRED', 'CANCELED'));

ALTER TABLE `intangible_assets`
    MODIFY COLUMN `intangible_asset_status` VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
        COMMENT '자산 상태 - 사용가능, 사용중, 만료, 해지완료';
