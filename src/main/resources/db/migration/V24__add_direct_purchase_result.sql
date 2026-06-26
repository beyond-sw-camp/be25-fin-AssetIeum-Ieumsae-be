CREATE TABLE IF NOT EXISTS `direct_purchase_results` (
	`ticket_id` CHAR(36) NOT NULL COMMENT 'Direct purchase ticket ID',
	`company_id` CHAR(36) NOT NULL COMMENT 'Company ID',
	`submitter_id` CHAR(36) NOT NULL COMMENT 'Submitter member ID',
	`actual_price` DECIMAL(15,2) NOT NULL COMMENT 'Actual payment amount',
	`purchase_date` DATETIME NOT NULL COMMENT 'Purchase date',
	`purchase_vendor` VARCHAR(150) NOT NULL COMMENT 'Purchase vendor',
	`serial_number` VARCHAR(100) NULL COMMENT 'Tangible asset serial number',
	`location` VARCHAR(150) NULL COMMENT 'Tangible asset location',
	`warranty_expired_at` DATETIME NULL COMMENT 'Tangible asset warranty expiration date',
	`license_code` VARCHAR(50) NULL COMMENT 'Intangible asset license code',
	`seat_count` INT NULL COMMENT 'Intangible asset seat count',
	`is_auto_renewal` TINYINT(1) NULL COMMENT 'Intangible asset auto renewal',
	`started_at` DATETIME NULL COMMENT 'Intangible asset usage start date',
	`expired_at` DATETIME NULL COMMENT 'Intangible asset expiration date',
	`billing_cycle` VARCHAR(30) NULL COMMENT 'Intangible asset billing cycle',
	`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
	`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated at',
	CONSTRAINT `PK_direct_purchase_results` PRIMARY KEY (`ticket_id`),
	CONSTRAINT `CK_direct_purchase_results_actual_price`
		CHECK (`actual_price` >= 0),
	CONSTRAINT `CK_direct_purchase_results_seat_count`
		CHECK (`seat_count` IS NULL OR `seat_count` >= 1),
	CONSTRAINT `CK_direct_purchase_results_billing_cycle`
		CHECK (`billing_cycle` IS NULL OR `billing_cycle` IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
	CONSTRAINT `CK_direct_purchase_results_asset_fields`
		CHECK (
			(
				`serial_number` IS NOT NULL
				AND `location` IS NOT NULL
				AND `warranty_expired_at` IS NOT NULL
				AND `license_code` IS NULL
				AND `seat_count` IS NULL
				AND `is_auto_renewal` IS NULL
				AND `started_at` IS NULL
				AND `expired_at` IS NULL
				AND `billing_cycle` IS NULL
			)
			OR
			(
				`serial_number` IS NULL
				AND `location` IS NULL
				AND `warranty_expired_at` IS NULL
				AND `license_code` IS NOT NULL
				AND `seat_count` IS NOT NULL
				AND `is_auto_renewal` IS NOT NULL
				AND `started_at` IS NOT NULL
				AND `expired_at` IS NOT NULL
				AND `billing_cycle` IS NOT NULL
			)
		),
	CONSTRAINT `CK_direct_purchase_results_intangible_period`
		CHECK (`started_at` IS NULL OR `expired_at` > `started_at`),
	CONSTRAINT `FK_direct_purchase_results_ticket_id_purchase_request_tickets`
		FOREIGN KEY (`ticket_id`) REFERENCES `purchase_request_tickets` (`ticket_id`),
	CONSTRAINT `FK_direct_purchase_results_company_id_companies`
		FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`),
	CONSTRAINT `FK_direct_purchase_results_submitter_id_members`
		FOREIGN KEY (`submitter_id`) REFERENCES `members` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `purchase_evidences`
	ADD COLUMN IF NOT EXISTS `ticket_id` CHAR(36) NULL COMMENT 'Purchase request ticket ID';

SET @request_id_exists := (
	SELECT COUNT(*)
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_evidences'
		AND COLUMN_NAME = 'request_id'
);

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_evidences'
		AND CONSTRAINT_NAME = 'FK_purchase_evidences_request_id_purchase_requests'
);

SET @sql := IF(
	@request_id_exists = 1 AND @constraint_exists = 1,
	'ALTER TABLE `purchase_evidences`
		DROP FOREIGN KEY `FK_purchase_evidences_request_id_purchase_requests`',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @request_id_nullable := (
	SELECT COALESCE(MAX(IS_NULLABLE), 'YES')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_evidences'
		AND COLUMN_NAME = 'request_id'
);

SET @sql := IF(
	@request_id_exists = 1 AND @request_id_nullable = 'NO',
	'ALTER TABLE `purchase_evidences`
		MODIFY COLUMN `request_id` CHAR(36) NULL COMMENT ''Legacy purchase request ID''',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_evidences'
		AND CONSTRAINT_NAME = 'FK_purchase_evidences_request_id_purchase_requests'
);

SET @sql := IF(
	@request_id_exists = 1 AND @constraint_exists = 0,
	'ALTER TABLE `purchase_evidences`
		ADD CONSTRAINT `FK_purchase_evidences_request_id_purchase_requests`
		FOREIGN KEY (`request_id`)
		REFERENCES `purchase_requests` (`request_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
	SELECT COUNT(*)
	FROM information_schema.TABLE_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
		AND TABLE_NAME = 'purchase_evidences'
		AND CONSTRAINT_NAME = 'FK_purchase_evidences_ticket_id_purchase_request_tickets'
);

SET @sql := IF(
	@constraint_exists = 0,
	'ALTER TABLE `purchase_evidences`
		ADD CONSTRAINT `FK_purchase_evidences_ticket_id_purchase_request_tickets`
		FOREIGN KEY (`ticket_id`)
		REFERENCES `purchase_request_tickets` (`ticket_id`)',
	'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
