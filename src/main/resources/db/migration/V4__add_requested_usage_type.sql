ALTER TABLE `asset_request_tickets`
	ADD COLUMN `requested_usage_type` VARCHAR(30) NOT NULL DEFAULT 'PERSONAL'
	COMMENT '요청 사용 유형 - PERSONAL: 개인 사용, DEPARTMENT: 부서 공용 사용'
	AFTER `request_method`;

ALTER TABLE `asset_request_tickets`
	ADD CONSTRAINT `CK_asset_request_tickets_requested_usage_type`
	CHECK (`requested_usage_type` IN ('PERSONAL', 'DEPARTMENT'));

ALTER TABLE `rental_tickets`
	ADD COLUMN `requested_usage_type` VARCHAR(30) NOT NULL DEFAULT 'PERSONAL'
	COMMENT '요청 사용 유형 - PERSONAL: 개인 사용, DEPARTMENT: 부서 공용 사용'
	AFTER `tangible_asset_id`;

ALTER TABLE `rental_tickets`
	ADD CONSTRAINT `CK_rental_tickets_requested_usage_type`
	CHECK (`requested_usage_type` IN ('PERSONAL', 'DEPARTMENT'));

ALTER TABLE `tangible_assets`
	ADD COLUMN `asset_usage_type` VARCHAR(30) NOT NULL DEFAULT 'PERSONAL'
	COMMENT '자산 사용 유형 - PERSONAL: 개인 사용, DEPARTMENT: 부서 공용 사용'
	AFTER `usage_type`;

ALTER TABLE `tangible_assets`
	ADD CONSTRAINT `CK_tangible_assets_asset_usage_type`
	CHECK (`asset_usage_type` IN ('PERSONAL', 'DEPARTMENT'));
