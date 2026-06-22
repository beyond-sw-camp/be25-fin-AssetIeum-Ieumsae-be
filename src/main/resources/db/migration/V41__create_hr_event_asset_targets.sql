CREATE TABLE `hr_event_asset_targets`
(
    `hr_event_asset_target_id` CHAR(36)     NOT NULL,
    `company_id`               CHAR(36)     NOT NULL,
    `hr_event_id`              CHAR(36)     NOT NULL,
    `member_id`                CHAR(36)     NOT NULL,
    `asset_type`               VARCHAR(30)  NOT NULL,
    `tangible_asset_id`        CHAR(36)     NULL,
    `intangible_asset_id`      CHAR(36)     NULL,
    `intangible_assignment_id` CHAR(36)     NULL,
    `action_type`              VARCHAR(50)  NOT NULL,
    `target_status`            VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    `processed_at`             DATETIME     NULL,
    `description`              VARCHAR(500) NULL,
    `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `PK_hr_event_asset_targets`
        PRIMARY KEY (`hr_event_asset_target_id`),
    CONSTRAINT `FK_hr_event_asset_targets_company_id_companies`
        FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`),
    CONSTRAINT `FK_hr_event_asset_targets_hr_event_id_hr_events`
        FOREIGN KEY (`hr_event_id`) REFERENCES `hr_events` (`hr_event_id`),
    CONSTRAINT `FK_hr_event_asset_targets_member_id_members`
        FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`),
    CONSTRAINT `FK_hr_event_asset_targets_tangible_asset_id_tangible_assets`
        FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`),
    CONSTRAINT `FK_hr_event_asset_targets_intangible_asset_id_intangible_assets`
        FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`),
    CONSTRAINT `FK_hr_event_asset_targets_intangible_assignment_id_intangible_asset_assignments`
        FOREIGN KEY (`intangible_assignment_id`)
            REFERENCES `intangible_asset_assignments` (`intangible_asset_assignment_id`),
    CONSTRAINT `CK_hr_event_asset_targets_asset_type_1`
        CHECK (`asset_type` IN ('TANGIBLE', 'INTANGIBLE')),
    CONSTRAINT `CK_hr_event_asset_targets_action_type_2`
        CHECK (`action_type` IN ('RETURN_REQUIRED', 'TRANSFER_REQUIRED', 'KEEP', 'UNASSIGN_REQUIRED')),
    CONSTRAINT `CK_hr_event_asset_targets_target_status_3`
        CHECK (`target_status` IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT `CK_hr_event_asset_targets_asset_xor_4`
        CHECK (((`tangible_asset_id` IS NOT NULL) + (`intangible_asset_id` IS NOT NULL)) = 1),
    CONSTRAINT `CK_hr_event_asset_targets_intangible_assignment_5`
        CHECK (`intangible_assignment_id` IS NULL OR `intangible_asset_id` IS NOT NULL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `UK_hr_event_asset_targets_tangible`
    ON `hr_event_asset_targets` (`hr_event_id`, `tangible_asset_id`);

CREATE UNIQUE INDEX `UK_hr_event_asset_targets_intangible`
    ON `hr_event_asset_targets` (`hr_event_id`, `intangible_asset_id`, `intangible_assignment_id`);

CREATE INDEX `IDX_hr_event_asset_targets_event_status`
    ON `hr_event_asset_targets` (`hr_event_id`, `target_status`);

CREATE INDEX `IDX_hr_event_asset_targets_member`
    ON `hr_event_asset_targets` (`member_id`);
