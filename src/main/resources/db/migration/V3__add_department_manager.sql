ALTER TABLE `departments`
	ADD COLUMN `department_manager_id` CHAR(36) NULL COMMENT '부서장 ID' AFTER `parent_department_id`;

ALTER TABLE `departments`
	ADD CONSTRAINT `FK_departments_department_manager_id_members`
	FOREIGN KEY (`department_manager_id`) REFERENCES `members` (`member_id`);
