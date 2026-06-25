UPDATE `hr_event_asset_targets`
SET `action_type` = 'RETURN_REQUIRED'
WHERE `action_type` = 'UNASSIGN_REQUIRED';

ALTER TABLE `hr_event_asset_targets`
    DROP CONSTRAINT `CK_hr_event_asset_targets_action_type_2`;

ALTER TABLE `hr_event_asset_targets`
    ADD CONSTRAINT `CK_hr_event_asset_targets_action_type_2`
        CHECK (`action_type` IN ('RETURN_REQUIRED', 'TRANSFER_REQUIRED', 'KEEP'));
