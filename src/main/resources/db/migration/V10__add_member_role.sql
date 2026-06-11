ALTER TABLE `members` DROP CONSTRAINT `CK_members_role_1`;

ALTER TABLE `members`
    ADD CONSTRAINT `CK_members_role_1`
        CHECK (`role` IN ('SUPER_ADMIN','ADMIN','DEPARTMENT_MANAGER','ASSET_MANAGER','ASSET_TEAM','EMPLOYEE'));

ALTER TABLE `purchase_request_tickets`
    MODIFY COLUMN `expected_price` DECIMAL(15,2) NULL COMMENT 'Expected price';