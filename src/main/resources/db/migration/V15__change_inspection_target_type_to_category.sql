ALTER TABLE inspections
    ADD COLUMN target_category_id CHAR(36) NULL COMMENT '조사 대상 카테고리 ID' AFTER target_department_id;

ALTER TABLE inspections
    DROP CONSTRAINT CK_inspections_target_type_2;

UPDATE inspections
SET target_type = 'CATEGORY'
WHERE target_type = 'ITEM';

ALTER TABLE inspections
    ADD CONSTRAINT CK_inspections_target_type_2
        CHECK (target_type IN ('ALL', 'DEPARTMENT', 'CATEGORY'));

ALTER TABLE inspections
    DROP CONSTRAINT CK_inspections_inspector_type_3;

UPDATE inspections
SET inspector_type = 'ASSET_TEAM'
WHERE inspector_type = 'ASSET_MANAGER';

ALTER TABLE inspections
    ADD CONSTRAINT CK_inspections_inspector_type_3
        CHECK (inspector_type IN ('EMPLOYEE', 'ASSET_TEAM'));
