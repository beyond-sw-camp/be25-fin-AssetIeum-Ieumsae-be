ALTER TABLE `notifications`
    DROP CONSTRAINT `CK_notifications_notification_type_1`;

ALTER TABLE `notifications`
    ADD CONSTRAINT `CK_notifications_notification_type_1`
        CHECK (`notification_type` IN (
            'TICKET_STATUS_CHANGED',
            'INSPECTION_STARTED',
            'INSPECTION_ENDING',
            'INSPECTION_REMINDER',
            'INTANGIBLE_ASSET_EXPIRED',
            'INTANGIBLE_ASSET_EXPIRING_TOMORROW',
            'INTANGIBLE_ASSET_PAYMENT_DUE',
            'RENTAL_RETURN_DUE_TOMORROW',
            'RENTAL_RETURN_DUE_TODAY',
            'RENTAL_RETURN_OVERDUE'
        ));
