ALTER TABLE direct_purchase_results
    MODIFY COLUMN serial_number TEXT NULL COMMENT 'Tangible asset serial numbers JSON';

ALTER TABLE direct_purchase_results
    MODIFY COLUMN license_code TEXT NULL COMMENT 'Intangible asset license codes JSON';
