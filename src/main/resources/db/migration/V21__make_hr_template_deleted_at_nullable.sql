ALTER TABLE `hr_templates`
    DROP CONSTRAINT `CK_hr_templates_template_type_1`,
    DROP COLUMN `template_type`,
    MODIFY COLUMN `deleted_at` DATETIME NULL DEFAULT NULL,
    ADD CONSTRAINT `UK_hr_templates_company_department` UNIQUE (`company_id`, `department_id`);
