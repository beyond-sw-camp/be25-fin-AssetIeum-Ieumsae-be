ALTER TABLE `tangible_assets`
    MODIFY COLUMN `asset_usage_type` VARCHAR(30) NULL DEFAULT NULL
        COMMENT 'Asset usage type - PERSONAL or DEPARTMENT';
