UPDATE `intangible_assets`
SET `intangible_asset_status` = 'CANCELLED'
WHERE `intangible_asset_status` = 'CANCELED';

ALTER TABLE `intangible_assets`
    DROP CONSTRAINT `CK_intangible_assets_intangible_asset_status_2`;

ALTER TABLE `intangible_assets`
    ADD CONSTRAINT `CK_intangible_assets_intangible_asset_status_2`
        CHECK (`intangible_asset_status` IN ('AVAILABLE', 'IN_USE', 'EXPIRED', 'CANCELLED'));
