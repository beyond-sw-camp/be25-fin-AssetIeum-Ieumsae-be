ALTER TABLE `asset_return_tickets`
    ADD COLUMN IF NOT EXISTS `processed_at` DATETIME(6) NULL COMMENT '반납/해지 처리 일시';
