SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tangible_asset_items'
      AND INDEX_NAME = 'UK_tangible_asset_items_item_code'
);

SET @sql := IF(@index_exists > 0,
    'ALTER TABLE `tangible_asset_items` DROP INDEX `UK_tangible_asset_items_item_code`',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tangible_asset_items'
      AND CONSTRAINT_NAME = 'UK_tangible_asset_items_company_product_name'
);

SET @sql := IF(@constraint_exists = 0,
    'ALTER TABLE `tangible_asset_items`
        ADD CONSTRAINT `UK_tangible_asset_items_company_product_name`
        UNIQUE (`company_id`, `product_name`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'intangible_asset_items'
      AND INDEX_NAME = 'UK_intangible_asset_items_item_code'
);

SET @sql := IF(@index_exists > 0,
    'ALTER TABLE `intangible_asset_items` DROP INDEX `UK_intangible_asset_items_item_code`',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'intangible_asset_items'
      AND CONSTRAINT_NAME = 'UK_intangible_asset_items_company_product_name'
);

SET @sql := IF(@constraint_exists = 0,
    'ALTER TABLE `intangible_asset_items`
        ADD CONSTRAINT `UK_intangible_asset_items_company_product_name`
        UNIQUE (`company_id`, `product_name`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tangible_assets'
      AND INDEX_NAME = 'UK_tangible_assets_serial_number'
);

SET @sql := IF(@index_exists > 0,
    'ALTER TABLE `tangible_assets` DROP INDEX `UK_tangible_assets_serial_number`',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tangible_assets'
      AND CONSTRAINT_NAME = 'UK_tangible_assets_company_serial_number'
);

SET @sql := IF(@constraint_exists = 0,
    'SELECT 1',
    'ALTER TABLE `tangible_assets` DROP INDEX `UK_tangible_assets_company_serial_number`'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tangible_assets'
      AND CONSTRAINT_NAME = 'UK_tangible_assets_company_item_serial_number'
);

SET @sql := IF(@constraint_exists = 0,
    'ALTER TABLE `tangible_assets`
        ADD CONSTRAINT `UK_tangible_assets_company_item_serial_number`
        UNIQUE (`company_id`, `tangible_item_id`, `serial_number`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'intangible_assets'
      AND INDEX_NAME = 'UK_intangible_assets_license_code'
);

SET @sql := IF(@index_exists > 0,
    'ALTER TABLE `intangible_assets` DROP INDEX `UK_intangible_assets_license_code`',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'intangible_assets'
      AND CONSTRAINT_NAME = 'UK_intangible_assets_company_license_code'
);

SET @sql := IF(@constraint_exists = 0,
    'SELECT 1',
    'ALTER TABLE `intangible_assets` DROP INDEX `UK_intangible_assets_company_license_code`'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'intangible_assets'
      AND CONSTRAINT_NAME = 'UK_intangible_assets_company_item_license_code'
);

SET @sql := IF(@constraint_exists = 0,
    'ALTER TABLE `intangible_assets`
        ADD CONSTRAINT `UK_intangible_assets_company_item_license_code`
        UNIQUE (`company_id`, `intangible_item_id`, `license_code`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
