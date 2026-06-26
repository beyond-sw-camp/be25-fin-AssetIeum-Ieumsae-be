ALTER TABLE `tangible_assets`
	DROP CONSTRAINT IF EXISTS `CK_tangible_assets_tangible_asset_status_2`;

ALTER TABLE `tangible_assets`
	ADD CONSTRAINT `CK_tangible_assets_tangible_asset_status_2`
	CHECK (`tangible_asset_status` IN (
		'AVAILABLE',
		'RESERVED',
		'IN_USE',
		'RETURN_REQUESTED',
		'REPAIR_REQUESTED',
		'REPAIRING',
		'DISPOSED'
	));

ALTER TABLE `rental_tickets`
	DROP CONSTRAINT IF EXISTS `CK_rental_tickets_rental_ticket_status_1`;

ALTER TABLE `rental_tickets`
	ADD CONSTRAINT `CK_rental_tickets_rental_ticket_status_1`
	CHECK (`rental_ticket_status` IN (
		'REQUESTED',
		'RESERVED',
		'ASSIGNED',
		'EXTENSION_REQUESTED',
		'COMPLETED',
		'CANCELLED'
	));

ALTER TABLE `asset_request_tickets`
	DROP CONSTRAINT IF EXISTS `CK_asset_request_tickets_asset_request_ticket_status_1`;

ALTER TABLE `asset_request_tickets`
	ADD CONSTRAINT `CK_asset_request_tickets_asset_request_ticket_status_1`
	CHECK (`asset_request_ticket_status` IN (
		'REQUESTED',
		'ASSIGNED',
		'COMPLETED',
		'CANCELLED'
	));
