ALTER TABLE `members`
	ADD CONSTRAINT `UK_members_company_id_member_no`
	UNIQUE (`company_id`, `member_no`);
