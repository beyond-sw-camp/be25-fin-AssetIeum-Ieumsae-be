ALTER TABLE `companies`
    ADD COLUMN `company_name` VARCHAR(100) NULL COMMENT 'Company name'
        AFTER `company_code`;

UPDATE `companies`
SET `company_name` = `company_code`
WHERE `company_name` IS NULL;

ALTER TABLE `companies`
    MODIFY COLUMN `company_name` VARCHAR(100) NOT NULL COMMENT 'Company name';

ALTER TABLE `companies`
    ADD CONSTRAINT `UK_companies_company_code`
        UNIQUE (`company_code`),
    ADD CONSTRAINT `UK_companies_company_name`
        UNIQUE (`company_name`);
