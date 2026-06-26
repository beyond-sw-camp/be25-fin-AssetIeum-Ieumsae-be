-- 자산이음 테이블 DDL

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `hr_template_items`;
DROP TABLE IF EXISTS `hr_templates`;
DROP TABLE IF EXISTS `hr_events`;
DROP TABLE IF EXISTS `budget_histories`;
DROP TABLE IF EXISTS `budgets`;
DROP TABLE IF EXISTS `purchase_evidences`;
DROP TABLE IF EXISTS `purchase_request_items`;
DROP TABLE IF EXISTS `purchase_requests`;
DROP TABLE IF EXISTS `purchase_policies`;
DROP TABLE IF EXISTS `files`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `activity_logs`;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `inspection_follow_ups`;
DROP TABLE IF EXISTS `inspection_results`;
DROP TABLE IF EXISTS `inspection_targets`;
DROP TABLE IF EXISTS `inspections`;
DROP TABLE IF EXISTS `intangible_asset_assignments`;
DROP TABLE IF EXISTS `intangible_assets`;
DROP TABLE IF EXISTS `intangible_asset_items`;
DROP TABLE IF EXISTS `intangible_asset_categories`;
DROP TABLE IF EXISTS `tangible_asset_assignments`;
DROP TABLE IF EXISTS `tangible_assets`;
DROP TABLE IF EXISTS `tangible_asset_items`;
DROP TABLE IF EXISTS `tangible_asset_categories`;
DROP TABLE IF EXISTS `ticket_comments`;
DROP TABLE IF EXISTS `purchase_return_tickets`;
DROP TABLE IF EXISTS `asset_return_tickets`;
DROP TABLE IF EXISTS `maintenance_tickets`;
DROP TABLE IF EXISTS `rental_tickets`;
DROP TABLE IF EXISTS `asset_request_tickets`;
DROP TABLE IF EXISTS `tickets`;
DROP TABLE IF EXISTS `members`;
DROP TABLE IF EXISTS `departments`;
DROP TABLE IF EXISTS `companies`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `companies` (
                             `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                             `company_code` VARCHAR(100) NOT NULL COMMENT '회사 코드',
                             `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                             `created_at` DATETIME NOT NULL COMMENT '생성 일시',
                             `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
                             CONSTRAINT `PK_companies` PRIMARY KEY (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `departments` (
                               `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                               `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                               `parent_department_id` CHAR(36) NULL COMMENT '상위 부서 ID',
                               `name` VARCHAR(100) NOT NULL COMMENT '부서명',
                               `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                               `created_at` DATETIME NOT NULL COMMENT '생성 일시',
                               `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
                               CONSTRAINT `PK_departments` PRIMARY KEY (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `members` (
                           `member_id` CHAR(36) NOT NULL COMMENT '멤버 ID',
                           `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                           `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                           `member_no` VARCHAR(100) NOT NULL COMMENT '사번',
                           `password` VARCHAR(100) NOT NULL COMMENT '비밀번호',
                           `name` VARCHAR(100) NOT NULL COMMENT '이름',
                           `role` VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE' COMMENT '역할',
                           `member_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
                           `email` VARCHAR(255) NULL COMMENT '이메일',
                           `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                           `created_at` DATETIME NOT NULL COMMENT '생성 일시',
                           `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
                           CONSTRAINT `PK_members` PRIMARY KEY (`member_id`),
                           CONSTRAINT `CK_members_role_1` CHECK (`role` IN ('SUPER_ADMIN','DEPARTMENT_MANAGER','ASSET_MANAGER','EMPLOYEE')),
                           CONSTRAINT `CK_members_member_status_2` CHECK (`member_status` IN ('ACTIVE','ON_LEAVE','RESIGNED')),
                           CONSTRAINT `UK_members_company_id_email` UNIQUE (`company_id`, `email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tickets` (
                           `ticket_id` CHAR(36) NOT NULL COMMENT '티켓 ID',
                           `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                           `ticket_no` VARCHAR(50) NOT NULL COMMENT '티켓 번호',
                           `ticket_type` VARCHAR(50) NOT NULL COMMENT '티켓 유형',
                           `ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '티켓 상태',
                           `requester_id` CHAR(36) NOT NULL COMMENT '요청자 ID',
                           `department_id` CHAR(36) NOT NULL COMMENT '요청 부서 ID',
                           `approver_id` CHAR(36) NOT NULL COMMENT '부서 책임자 ID',
                           `assignee_id` CHAR(36) NULL COMMENT '구매자산팀 담당자 ID',
                           `request_reason` VARCHAR(255) NULL COMMENT '요청 사유',
                           `department_approved_at` DATETIME NULL COMMENT '부서 책임자 승인 일시',
                           `department_rejected_at` DATETIME NULL COMMENT '부서 책임자 반려 일시',
                           `department_rejection_reason` VARCHAR(255) NULL COMMENT '부서 책임자 반려 사유',
                           `purchase_approved_at` DATETIME NULL COMMENT '구매자산팀 승인 일시',
                           `purchase_rejected_at` DATETIME NULL COMMENT '구매자산팀 반려 일시',
                           `purchase_rejection_reason` VARCHAR(255) NULL COMMENT '구매자산팀 반려 사유',
                           `completed_at` DATETIME NULL COMMENT '완료 일시',
                           `cancelled_at` DATETIME NULL COMMENT '취소 일시',
                           `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                           `created_at` DATETIME NOT NULL COMMENT '생성 일시',
                           `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
                           CONSTRAINT `PK_tickets` PRIMARY KEY (`ticket_id`),
                           CONSTRAINT `UK_tickets_ticket_no` UNIQUE (`ticket_no`),
                           CONSTRAINT `CK_tickets_ticket_type_1` CHECK (`ticket_type` IN ('ASSET_REQUEST','RENTAL','RENTAL_EXTENSION','MAINTENANCE_REQUEST','ASSET_RETURN', 'PURCHASE_REQUEST', 'PURCHASE_RETURN')),
                           CONSTRAINT `CK_tickets_ticket_status_2` CHECK (`ticket_status` IN ('REQUESTED','DEPARTMENT_APPROVED','DEPARTMENT_REJECTED','ASSET_APPROVED','ASSET_REJECTED','IN_PROGRESS','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `asset_request_tickets` (
                                         `ticket_id` CHAR(36) NOT NULL COMMENT '자산 요청 티켓 ID',
                                         `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                         `asset_request_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '상태',
                                         `request_method` VARCHAR(50) NOT NULL COMMENT '요청 처리 방식',
                                         `tangible_asset_item_id` CHAR(36) NULL COMMENT '유형 자산 품목 ID',
                                         `intangible_asset_item_id` CHAR(36) NULL COMMENT '무형 자산 품목 ID',
                                         `tangible_asset_id` CHAR(36) NULL COMMENT '할당 유형 자산 ID',
                                         `intangible_asset_id` CHAR(36) NULL COMMENT '할당 무형 자산 ID',
                                         `requested_item_name` VARCHAR(100) NULL COMMENT '미등록 품목명',
                                         `expected_price` DECIMAL(15,2) NULL COMMENT '예상 금액',
                                         `actual_price` DECIMAL(15,2) NULL COMMENT '실제 결제 금액',
                                         `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                                         CONSTRAINT `PK_asset_request_tickets` PRIMARY KEY (`ticket_id`),
                                         CONSTRAINT `CK_asset_request_tickets_asset_request_ticket_status_1` CHECK (`asset_request_ticket_status` IN ('REQUESTED','ASSIGNED','COMPLETED')),
                                         CONSTRAINT `CK_asset_request_tickets_request_method_2` CHECK (`request_method` IN ('TEAM_PURCHASE','DIRECT_PURCHASE')),
                                         CONSTRAINT `CK_asset_request_tickets_XOR_3` CHECK (((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL) + (`requested_item_name` IS NOT NULL)) = 1),
                                         CONSTRAINT `CK_asset_request_tickets_XOR_4` CHECK (((`tangible_asset_id` IS NOT NULL) + (`intangible_asset_id` IS NOT NULL)) = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rental_tickets` (
                                  `ticket_id` CHAR(36) NOT NULL COMMENT '대여 티켓 ID',
                                  `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                  `rental_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '상태',
                                  `tangible_asset_id` CHAR(36) NULL COMMENT '대여 유형자산 ID',
                                  `rental_start_date` DATETIME NOT NULL COMMENT '대여 시작일',
                                  `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                                  `rental_due_date` DATETIME NULL COMMENT '반납 예정일시',
                                  `requested_due_date` DATETIME NULL COMMENT '요청 반납 예정일시',
                                  CONSTRAINT `PK_rental_tickets` PRIMARY KEY (`ticket_id`),
                                  CONSTRAINT `CK_rental_tickets_rental_ticket_status_1` CHECK (`rental_ticket_status` IN ('REQUESTED','ASSIGNED','EXTENSION_REQUESTED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `maintenance_tickets` (
                                       `ticket_id` CHAR(36) NOT NULL COMMENT '유지보수 티켓 ID',
                                       `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                       `maintenance_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '상태',
                                       `tangible_asset_id` CHAR(36) NOT NULL COMMENT '유형 자산 ID',
                                       `collected_at` DATETIME NULL COMMENT '자산 회수 일시',
                                       `maintenance_result` VARCHAR(255) NULL COMMENT '유지보수 결과',
                                       `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                                       `maintenance_completed_at` DATETIME NULL COMMENT '유지보수 완료 일시',
                                       `maintenance_cost` DECIMAL(15,2) NULL COMMENT '유지보수 비용',
                                       CONSTRAINT `PK_maintenance_tickets` PRIMARY KEY (`ticket_id`),
                                       CONSTRAINT `CK_maintenance_tickets_maintenance_ticket_status_1` CHECK (`maintenance_ticket_status` IN ('REQUESTED','COLLECTED','IN_PROGRESS','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `asset_return_tickets` (
                                        `ticket_id` CHAR(36) NOT NULL COMMENT '반납 티켓 ID',
                                        `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                        `asset_return_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '상태',
                                        `intangible_asset_id` CHAR(36) NULL COMMENT '무형 자산 ID',
                                        `tangible_asset_id` CHAR(36) NULL COMMENT '유형 자산 ID',
                                        `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                                        `collected_at` DATETIME NULL COMMENT '자산 회수 일시',
                                        CONSTRAINT `PK_asset_return_tickets` PRIMARY KEY (`ticket_id`),
                                        CONSTRAINT `CK_asset_return_tickets_asset_return_ticket_status_1` CHECK (`asset_return_ticket_status` IN ('REQUESTED','COLLECTED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `purchase_return_tickets` (
                                           `ticket_id` CHAR(36) NOT NULL COMMENT '반품 티켓 ID',
                                           `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                           `purchase_return_type` VARCHAR(50) NOT NULL COMMENT '유형',
                                           `purchase_return_ticket_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' COMMENT '상태',
                                           `intangible_asset_id` CHAR(36) NULL COMMENT '무형 자산 ID',
                                           `tangible_asset_id` CHAR(36) NULL COMMENT '유형 자산 ID',
                                           `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시',
                                           `collected_at` DATETIME NULL COMMENT '자산 회수 일시',
                                           `shipped_at` DATETIME NULL COMMENT '반품 발송 일시',
                                           CONSTRAINT `PK_purchase_return_tickets` PRIMARY KEY (`ticket_id`),
                                           CONSTRAINT `CK_purchase_return_tickets_purchase_return_type_1` CHECK (`purchase_return_type` IN ('DIRECT_RETURN','ASSET_MANAGER_RETURN')),
                                           CONSTRAINT `CK_purchase_return_tickets_purchase_return_ticket_status_2` CHECK (`purchase_return_ticket_status` IN ('REQUESTED','COLLECTED','SHIPPED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ticket_comments` (
                                   `ticket_comment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 ID',
                                   `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                   `ticket_id` CHAR(36) NOT NULL COMMENT '티켓 ID',
                                   `writer_id` CHAR(36) NOT NULL COMMENT '작성자 ID',
                                   `content` VARCHAR(255) NOT NULL COMMENT '댓글 내용',
                                   `created_at` DATETIME NOT NULL COMMENT '작성 일시',
                                   `updated_at` DATETIME NOT NULL COMMENT '수정 일시',
                                   `deleted_at` DATETIME NULL COMMENT '삭제 일시',
                                   CONSTRAINT `PK_ticket_comments` PRIMARY KEY (`ticket_comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tangible_asset_categories` (
                                             `tangible_asset_category_id` CHAR(36) NOT NULL COMMENT '유형자산 카테고리 ID',
                                             `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                             `parent_id` CHAR(36) NULL COMMENT '상위 카테고리 ID - 대분류/중분류 구조를 위한 자기참조 FK',
                                             `name` VARCHAR(100) NOT NULL COMMENT '카테고리명',
                                             `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시 - NULL이면 삭제되지 않은 데이터 값이 있으면 삭제 처리된 데이터',
                                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                             CONSTRAINT `PK_tangible_asset_categories` PRIMARY KEY (`tangible_asset_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tangible_asset_items` (
                                        `tangible_asset_item_id` CHAR(36) NOT NULL COMMENT '유형자산 품목 ID',
                                        `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                        `category_id` CHAR(36) NOT NULL COMMENT '유형자산 카테고리 ID',
                                        `item_code` VARCHAR(50) NOT NULL COMMENT '품목번호 - 품목을 식별하는 고유 번호',
                                        `manufacturer` VARCHAR(100) NULL COMMENT '제조사 - 예: LG, Apple, Samsung',
                                        `model_name` VARCHAR(100) NULL COMMENT '모델명 - 예: 16Z90R, MBP14',
                                        `is_standard` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '표준 품목 여부 - 표준 유형자산 요청 목록 노출 여부',
                                        `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시 - NULL이면 삭제되지 않은 데이터 값이 있으면 삭제 처리된 데이터',
                                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                        CONSTRAINT `PK_tangible_asset_items` PRIMARY KEY (`tangible_asset_item_id`),
                                        CONSTRAINT `UK_tangible_asset_items_item_code` UNIQUE (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tangible_assets` (
                                   `tangible_asset_id` CHAR(36) NOT NULL COMMENT '유형자산 ID',
                                   `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                   `tangible_item_id` CHAR(36) NOT NULL COMMENT '유형자산 품목 ID',
                                   `asset_code` VARCHAR(50) NOT NULL COMMENT '자산번호',
                                   `serial_number` VARCHAR(100) NOT NULL COMMENT '시리얼번호',
                                   `usage_type` VARCHAR(30) NULL COMMENT '사용유형 - 사용중일 때만 정식 배정/임시 대여 구분',
                                   `tangible_asset_status` VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' COMMENT '자산 상태 - 사용가능, 사용중, 반납요청, 수리요청, 수리중, 폐기완료',
                                   `member_id` CHAR(36) NULL COMMENT '현재 사용자 ID - 사용가능 상태면 사용자 없음',
                                   `department_id` CHAR(36) NULL COMMENT '사용 부서 ID - 사용가능 상태면 부서가 없을 수 있음',
                                   `location` VARCHAR(150) NULL COMMENT '위치',
                                   `used_started_at` DATETIME NULL COMMENT '사용 시작일시',
                                   `return_due_date` DATETIME NULL COMMENT '반납 예정일시',
                                   `purchase_date` DATETIME NULL COMMENT '구매 일시 - 기존 자산 이관 시 모를 수 있음',
                                   `purchase_price` DECIMAL(15,2) NULL COMMENT '구매금액',
                                   `purchase_vendor` VARCHAR(150) NULL COMMENT '구매처',
                                   `warranty_expired_at` DATETIME NULL COMMENT '보증만료일',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                   `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                   CONSTRAINT `PK_tangible_assets` PRIMARY KEY (`tangible_asset_id`),
                                   CONSTRAINT `UK_tangible_assets_asset_code` UNIQUE (`asset_code`),
                                   CONSTRAINT `UK_tangible_assets_serial_number` UNIQUE (`serial_number`),
                                   CONSTRAINT `CK_tangible_assets_usage_type_1` CHECK (`usage_type` IN ('TEMPORARY', 'PERMANENT')),
                                   CONSTRAINT `CK_tangible_assets_tangible_asset_status_2` CHECK (`tangible_asset_status` IN ('AVAILABLE', 'IN_USE', 'RETURN_REQUESTED', 'REPAIR_REQUESTED', 'REPAIRING', 'DISPOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tangible_asset_assignments` (
                                              `tangible_asset_assignment_id` CHAR(36) NOT NULL COMMENT '유형자산 배정 이력 ID - 유형자산 배정 이력 식별자',
                                              `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                              `tangible_asset_id` CHAR(36) NOT NULL COMMENT '유형자산 ID - 배정 또는 대여된 유형자산',
                                              `member_id` CHAR(36) NOT NULL COMMENT '사용자 ID',
                                              `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                                              `assignment_type` VARCHAR(30) NOT NULL COMMENT '배정 유형 - 정식 배정 또는 임시 대여',
                                              `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작 일시',
                                              `return_due_date` DATETIME NULL COMMENT '종료 예정일시',
                                              `ended_at` DATETIME NULL COMMENT '종료 일시',
                                              `assignment_status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '배정 이력 상태 - ACTIVE, ENDED',
                                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                              CONSTRAINT `PK_tangible_asset_assignments` PRIMARY KEY (`tangible_asset_assignment_id`),
                                              CONSTRAINT `CK_tangible_asset_assignments_assignment_type_1` CHECK (`assignment_type` IN ('TEMPORARY', 'PERMANENT')),
                                              CONSTRAINT `CK_tangible_asset_assignments_assignment_status_2` CHECK (`assignment_status` IN ('ACTIVE', 'ENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `intangible_asset_categories` (
                                               `intangible_asset_category_id` CHAR(36) NOT NULL COMMENT '무형자산 카테고리 ID',
                                               `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                               `parent_id` CHAR(36) NULL COMMENT '상위 카테고리 ID - 대분류/중분류 구조를 위한 자기참조 FK',
                                               `name` VARCHAR(100) NOT NULL COMMENT '카테고리명',
                                               `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시 - NULL이면 삭제되지 않은 데이터 값이 있으면 삭제 처리된 데이터',
                                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                               `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                               CONSTRAINT `PK_intangible_asset_categories` PRIMARY KEY (`intangible_asset_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `intangible_asset_items` (
                                          `intangible_asset_item_id` CHAR(36) NOT NULL COMMENT '무형자산 품목 ID',
                                          `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                          `category_id` CHAR(36) NOT NULL COMMENT '무형자산 카테고리 ID',
                                          `item_code` VARCHAR(50) NOT NULL COMMENT '품목번호 - 품목을 식별하는 고유 번호',
                                          `name` VARCHAR(150) NOT NULL COMMENT '소프트웨어명 - 예: Microsoft 365, Adobe Photoshop, Figma',
                                          `provider` VARCHAR(100) NULL COMMENT '제공사 - 예: Microsoft, Adobe, Figma',
                                          `is_standard` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '표준 품목 여부 - 표준 무형자산 요청 목록 노출 여부',
                                          `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 일시 - NULL이면 삭제되지 않은 데이터 값이 있으면 삭제 처리된 데이터',
                                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                          `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                          CONSTRAINT `PK_intangible_asset_items` PRIMARY KEY (`intangible_asset_item_id`),
                                          CONSTRAINT `UK_intangible_asset_items_item_code` UNIQUE (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `intangible_assets` (
                                     `intangible_asset_id` CHAR(36) NOT NULL COMMENT '무형자산 ID',
                                     `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                     `intangible_item_id` CHAR(36) NOT NULL COMMENT '무형자산 품목 ID',
                                     `license_code` VARCHAR(50) NOT NULL COMMENT '라이선스 번호 - 시스템 내 라이선스 고유 번호',
                                     `license_type` VARCHAR(30) NOT NULL COMMENT '라이선스 유형 - 구독형, 영구형, 기간제',
                                     `intangible_asset_status` VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' COMMENT '자산 상태 - 사용가능, 사용중, 만료예정, 만료, 해지요청, 해지완료',
                                     `member_id` CHAR(36) NULL COMMENT '현재 사용자 ID',
                                     `department_id` CHAR(36) NULL COMMENT '사용 부서 ID',
                                     `seat_count` INT NOT NULL COMMENT '최대 사용 가능 인원 수',
                                     `started_at` DATETIME NULL COMMENT '사용 시작일시',
                                     `expired_at` DATETIME NULL COMMENT '만료일시',
                                     `is_auto_renewal` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '자동 연장 여부 - 자동 갱신 여부',
                                     `billing_cycle` VARCHAR(30) NULL COMMENT '결제 주기 - 월간, 연간, 일회성',
                                     `purchase_date` DATETIME NULL COMMENT '구매 일시',
                                     `purchase_price` DECIMAL(15,2) NULL COMMENT '구매금액',
                                     `purchase_vendor` VARCHAR(150) NULL COMMENT '구매처',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                     CONSTRAINT `PK_intangible_assets` PRIMARY KEY (`intangible_asset_id`),
                                     CONSTRAINT `UK_intangible_assets_license_code` UNIQUE (`license_code`),
                                     CONSTRAINT `CK_intangible_assets_license_type_1` CHECK (`license_type` IN ('SUBSCRIPTION', 'PERPETUAL', 'TERM')),
                                     CONSTRAINT `CK_intangible_assets_intangible_asset_status_2` CHECK (`intangible_asset_status` IN ('AVAILABLE', 'IN_USE', 'EXPIRING_SOON', 'EXPIRED', 'CANCEL_REQUESTED', 'CANCELED')),
                                     CONSTRAINT `CK_intangible_assets_seat_count_3` CHECK (seat_count >= 1),
                                     CONSTRAINT `CK_intangible_assets_billing_cycle_4` CHECK (`billing_cycle` IN ('MONTHLY', 'YEARLY', 'ONE_TIME'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `intangible_asset_assignments` (
                                                `intangible_asset_assignment_id` CHAR(36) NOT NULL COMMENT '무형자산 배정 이력 ID - 무형자산 배정 이력 식별자',
                                                `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                                `intangible_asset_id` CHAR(36) NOT NULL COMMENT '무형자산 ID',
                                                `member_id` CHAR(36) NOT NULL COMMENT '사용자 ID',
                                                `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                                                `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작 일시',
                                                `ended_at` DATETIME NULL COMMENT '종료 일시',
                                                `assignment_status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '배정 이력 상태 - ACTIVE, ENDED',
                                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                                CONSTRAINT `PK_intangible_asset_assignments` PRIMARY KEY (`intangible_asset_assignment_id`),
                                                CONSTRAINT `CK_intangible_asset_assignments_assignment_status_1` CHECK (`assignment_status` IN ('ACTIVE', 'ENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inspections` (
                               `inspection_id` CHAR(36) NOT NULL COMMENT '전수조사 ID',
                               `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                               `inspection_type` VARCHAR(50) NOT NULL COMMENT '조사 유형',
                               `target_type` VARCHAR(50) NOT NULL COMMENT '조사 대상 유형',
                               `target_department_id` CHAR(36) NULL COMMENT '조사 대상 부서 ID',
                               `inspector_type` VARCHAR(50) NOT NULL COMMENT '조사 수행자 유형 - 사원 직접 조사 / 자산관리팀 조사',
                               `start_date` DATETIME NOT NULL COMMENT '전수조사 시작 일시',
                               `end_date` DATETIME NOT NULL COMMENT '전수조사 종료 일시',
                               `inspection_status` VARCHAR(50) NOT NULL DEFAULT 'READY' COMMENT '전수조사 상태',
                               `description` VARCHAR(500) NULL COMMENT '전수조사 설명',
                               `inspector_id` CHAR(36) NOT NULL COMMENT '담당자',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                               `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                               CONSTRAINT `PK_inspections` PRIMARY KEY (`inspection_id`),
                               CONSTRAINT `CK_inspections_inspection_type_1` CHECK (`inspection_type` IN ('TANGIBLE_ASSET', 'INTANGIBLE_ASSET')),
                               CONSTRAINT `CK_inspections_target_type_2` CHECK (`target_type` IN ('ALL', 'DEPARTMENT', 'ITEM')),
                               CONSTRAINT `CK_inspections_inspector_type_3` CHECK (`inspector_type` IN ('EMPLOYEE', 'ASSET_MANAGER')),
                               CONSTRAINT `CK_inspections_end_date_4` CHECK (end_date >= start_date),
                               CONSTRAINT `CK_inspections_inspection_status_5` CHECK (`inspection_status` IN ('READY', 'IN_PROGRESS', 'COMPLETED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inspection_targets` (
                                      `inspection_target_id` CHAR(36) NOT NULL COMMENT '조사 대상 ID',
                                      `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                      `inspection_id` CHAR(36) NOT NULL COMMENT '전수조사 ID',
                                      `tangible_asset_id` CHAR(36) NULL COMMENT '유형자산 ID',
                                      `intangible_asset_id` CHAR(36) NULL COMMENT '무형자산 ID',
                                      `is_responded` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '사용자 응답 여부 - 자산 조사 시(유형 + 무형)',
                                      `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                      `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                      CONSTRAINT `PK_inspection_targets` PRIMARY KEY (`inspection_target_id`),
                                      CONSTRAINT `CK_inspection_targets_XOR_1` CHECK (((`tangible_asset_id` IS NOT NULL) + (`intangible_asset_id` IS NOT NULL)) = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inspection_results` (
                                      `inspection_result_id` CHAR(36) NOT NULL COMMENT '조사 결과 ID',
                                      `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                      `inspection_id` CHAR(36) NOT NULL COMMENT '전수조사 ID',
                                      `inspection_target_id` CHAR(36) NOT NULL COMMENT '조사 대상 ID',
                                      `follow_up_requests` TINYINT(1) NOT NULL COMMENT '후속 처리 필요 여부 - 1: 후속 처리 필요, 0: 후속 처리 불필요',
                                      `response_content` TEXT NULL COMMENT '사용자 응답 내용 - 유형, 무형 자산에 대한 사용자 응답 내용',
                                      `reviewer_id` CHAR(36) NULL COMMENT '검토자',
                                      `checked_at` DATETIME NULL COMMENT '검토 일시',
                                      `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                      `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                      CONSTRAINT `PK_inspection_results` PRIMARY KEY (`inspection_result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inspection_follow_ups` (
                                         `follow_up_id` CHAR(36) NOT NULL COMMENT '후속 처리 ID',
                                         `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                         `inspection_result_id` CHAR(36) NOT NULL COMMENT '조사 결과 ID',
                                         `action_detail` TEXT NULL COMMENT '처리 내용',
                                         `processor_id` CHAR(36) NULL COMMENT '처리자',
                                         `processed_at` DATETIME NULL COMMENT '처리 일시',
                                         `inspection_follow_up_status` VARCHAR(50) NULL COMMENT '처리 상태',
                                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                         `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                         CONSTRAINT `PK_inspection_follow_ups` PRIMARY KEY (`follow_up_id`),
                                         CONSTRAINT `CK_inspection_follow_ups_inspection_follow_up_status_1` CHECK (`inspection_follow_up_status` IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `audit_logs` (
                              `audit_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '감사 로그 ID',
                              `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                              `log_type` VARCHAR(50) NOT NULL COMMENT '로그 유형',
                              `target_type` VARCHAR(50) NOT NULL COMMENT '연결 대상 타입',
                              `target_id` CHAR(36) NOT NULL COMMENT '연결 대상 ID',
                              `before_value` TEXT NOT NULL COMMENT '변경 전 값',
                              `after_value` TEXT NOT NULL COMMENT '변경 후 값',
                              `member_id` CHAR(36) NOT NULL COMMENT '수행자',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                              CONSTRAINT `PK_audit_logs` PRIMARY KEY (`audit_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `activity_logs` (
                                 `activity_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '활동 로그 ID',
                                 `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                 `member_id` CHAR(36) NOT NULL COMMENT '활동자 - 활동 사용자',
                                 `activity_type` VARCHAR(50) NOT NULL COMMENT '활동 유형',
                                 `target_type` VARCHAR(50) NOT NULL COMMENT '연결 대상 타입',
                                 `target_id` CHAR(36) NOT NULL COMMENT '연결 대상 ID',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                 CONSTRAINT `PK_activity_logs` PRIMARY KEY (`activity_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
                                 `notification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 ID',
                                 `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                 `receiver_id` CHAR(36) NOT NULL COMMENT '수신자 ID',
                                 `notification_type` VARCHAR(50) NOT NULL COMMENT '알림 유형',
                                 `title` VARCHAR(200) NOT NULL COMMENT '알림 제목',
                                 `content` TEXT NOT NULL COMMENT '알림 내용',
                                 `target_type` VARCHAR(50) NOT NULL COMMENT '연결 대상 타입',
                                 `target_id` CHAR(36) NOT NULL COMMENT '연결 대상 ID - ticket_id, inspection_id,',
                                 `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '읽음 여부',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                 CONSTRAINT `PK_notifications` PRIMARY KEY (`notification_id`),
                                 CONSTRAINT `CK_notifications_notification_type_1` CHECK (`notification_type` IN ('TICKET_STATUS_CHANGED', 'INSPECTION_STARTED', 'INSPECTION_ENDING', 'INSPECTION_REMINDER', 'SOFTWARE_ASSET_EXPIRED')),
                                 CONSTRAINT `CK_notifications_target_type_2` CHECK (`target_type` IN ('TICKET', 'INSPECTION', 'TANGIBLE_ASSET', 'INTANGIBLE_ASSET'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `files` (
                         `file_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '파일 ID',
                         `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                         `target_type` VARCHAR(50) NOT NULL COMMENT '연결 대상 타입',
                         `target_id` CHAR(36) NOT NULL COMMENT '연결 대상 ID',
                         `name` VARCHAR(255) NOT NULL COMMENT '파일명',
                         `path` VARCHAR(500) NOT NULL COMMENT '파일 경로',
                         `file_size` BIGINT NOT NULL COMMENT '파일 크기',
                         `extension` VARCHAR(20) NOT NULL COMMENT '파일 확장자',
                         `uploader_id` CHAR(36) NOT NULL COMMENT '업로더_ID',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                         CONSTRAINT `PK_files` PRIMARY KEY (`file_id`),
                         CONSTRAINT `CK_files_file_size_1` CHECK (file_size >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `purchase_policies` (
                                     `policy_id` CHAR(36) NOT NULL COMMENT '정책 고유 ID',
                                     `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                     `is_exclusive_asset_team` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '구매자산팀 전담 여부 - 1: 전담, 0: 제외',
                                     `allow_direct_purchase` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '직접구매 허용 여부 - 사원 기준',
                                     `allow_parallel_operation` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '병행 운영 여부',
                                     `over_percentage_limit` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '초과 허용 범위 - 실제 결제 금액 초과 허용 범위 % 단위,  예: 10.00 = 10% 초과 허용',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                     CONSTRAINT `PK_purchase_policies` PRIMARY KEY (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `purchase_requests` (
                                     `request_id` CHAR(36) NOT NULL COMMENT '구매 요청 고유 ID',
                                     `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                     `requester_id` CHAR(36) NOT NULL COMMENT '신청자 ID',
                                     `purchase_request_status` VARCHAR(20) NOT NULL COMMENT '상태',
                                     `estimated_amount` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '예정 금액 - 부서장 승인 및 집행대기금액 기준 금액',
                                     `actual_amount` DECIMAL(15,2) NULL COMMENT '실제 결제 금액',
                                     `ordered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발주 일시',
                                     `delivery_date` DATETIME NULL COMMENT '납품 완료일',
                                     `approved_at` DATETIME NOT NULL COMMENT '승인 일시',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                     CONSTRAINT `PK_purchase_requests` PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `purchase_request_items` (
                                          `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '품목 고유 ID',
                                          `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                          `request_id` CHAR(36) NOT NULL COMMENT '구매 요청 고유 ID',
                                          `intangible_asset_item_id` CHAR(36) NULL COMMENT '무형 자산 품목 ID',
                                          `tangible_asset_item_id` CHAR(36) NULL COMMENT '유형 자산 품목 ID',
                                          `item_name` VARCHAR(255) NOT NULL COMMENT '품목명 - 예: 맥북, 특수 그래픽 태블릿',
                                          `department_id` CHAR(36) NOT NULL COMMENT '부서 ID - 비용을 청구할 대상 부서',
                                          `is_standard` TINYINT(1) NOT NULL COMMENT '표준 품목 여부 - 1: 전사비용/Hold없음, 0: 부서비용/Hold필수',
                                          `quantity` INT NOT NULL COMMENT '수량',
                                          `estimated_unit_price` DECIMAL(15,2) NOT NULL COMMENT '예정 단가',
                                          `actual_unit_price` DECIMAL(15,2) NULL COMMENT '실제 결제 단가',
                                          `external_url` VARCHAR(500) NULL COMMENT '외부 URL - 비표준 품목의 경우 외부 쇼핑몰 링크',
                                          `received_at` DATETIME NULL COMMENT '입고 일시',
                                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                          `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                                          CONSTRAINT `PK_purchase_request_items` PRIMARY KEY (`item_id`),
                                          CONSTRAINT `CK_purchase_request_items_XOR_1` CHECK (((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL)) = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `purchase_evidences` (
                                      `evidence_id` CHAR(36) NOT NULL COMMENT '증빙 고유 ID',
                                      `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                      `request_id` CHAR(36) NOT NULL COMMENT '구매 요청 고유 ID',
                                      `intangible_asset_item_id` CHAR(36) NULL COMMENT '무형 자산 품목 ID',
                                      `tangible_asset_item_id` CHAR(36) NULL COMMENT '유형 자산 품목 ID',
                                      `evidence_type` VARCHAR(20) NOT NULL COMMENT '증빙 종류',
                                      `file_id` BIGINT NOT NULL COMMENT '파일 ID',
                                      `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',
                                      CONSTRAINT `PK_purchase_evidences` PRIMARY KEY (`evidence_id`),
                                      CONSTRAINT `CK_purchase_evidences_evidence_type_1` CHECK (`evidence_type` IN ('RECEIPT', 'INVOICE', 'TAX_INVOICE', 'CARD_APPROVAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `budgets` (
                           `budget_id` CHAR(36) NOT NULL COMMENT '예산 고유 ID',
                           `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                           `budget_year` INT NOT NULL COMMENT '예산 해당 연도',
                           `department_id` CHAR(36) NULL DEFAULT NULL COMMENT '부서 고유 ID - 전사 공통 계정일 경우 0',
                           `total_amount` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '연간 총 예산 금액',
                           `held_amount` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '집행 대기 금액',
                           `used_amount` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '실제 사용 금액',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 일시',
                           CONSTRAINT `PK_budgets` PRIMARY KEY (`budget_id`),
                           CONSTRAINT `UK_budgets_budget_year_department_id` UNIQUE (`budget_year`, `department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `budget_histories` (
                                    `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
                                    `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                    `department_id` CHAR(36) NULL COMMENT '부서 ID',
                                    `budget_id` CHAR(36) NOT NULL COMMENT '예산 ID',
                                    `history_type` VARCHAR(30) NOT NULL COMMENT '유형',
                                    `amount` DECIMAL(15,2) NOT NULL COMMENT '변동 금액',
                                    `used_amount_before` DECIMAL(15,2) NOT NULL COMMENT '실제 사용 예산 변동 전 금액',
                                    `used_amount_after` DECIMAL(15,2) NOT NULL COMMENT '실제 사용 예산 변동 후 금액',
                                    `hold_amount_before` DECIMAL(15,2) NOT NULL COMMENT '집행 대기 금액 변동 전',
                                    `hold_amount_after` DECIMAL(15,2) NOT NULL COMMENT '집행 대기 금액 변동 후',
                                    `total_budget` DECIMAL(15,2) NOT NULL COMMENT '총 예산',
                                    `description` VARCHAR(500) NULL COMMENT '세부 변동 사유 - 예: 맥북 구매 승인으로 인한 대기금액 선점',
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                    CONSTRAINT `PK_budget_histories` PRIMARY KEY (`history_id`),
                                    CONSTRAINT `CK_budget_histories_history_type_1` CHECK (`history_type` IN ('HOLD_INCREASE', 'HOLD_DECREASE', 'USE_INCREASE', 'RECOVERY', 'TRANSFER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `hr_events` (
                             `hr_event_id` CHAR(36) NOT NULL COMMENT 'HR 이벤트 ID',
                             `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                             `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                             `member_id` CHAR(36) NOT NULL COMMENT '멤버 ID - 이벤트 대상 사원',
                             `event_type` VARCHAR(100) NOT NULL COMMENT '유형',
                             `hr_event_status` VARCHAR(100) NOT NULL DEFAULT 'PENDING' COMMENT '상태',
                             `event_date` DATE NOT NULL COMMENT '예정일',
                             `executed_at` DATETIME NOT NULL COMMENT '실행일',
                             `completed_at` DATETIME NULL DEFAULT NULL COMMENT '완료일',
                             `cancelled_at` DATETIME NULL DEFAULT NULL COMMENT '취소일',
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일',
                             CONSTRAINT `PK_hr_events` PRIMARY KEY (`hr_event_id`),
                             CONSTRAINT `CK_hr_events_event_type_1` CHECK (`event_type` IN ('ONBOARDING','OFFBOARDING','DEPARTMENT_TRANSFER','LEAVE','RETURN')),
                             CONSTRAINT `CK_hr_events_hr_event_status_2` CHECK (`hr_event_status` IN ('PENDING','IN_PROGRESS','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `hr_templates` (
                                `hr_template_id` CHAR(36) NOT NULL COMMENT 'HR 템플릿 ID',
                                `company_id` CHAR(36) NOT NULL COMMENT '회사 ID',
                                `department_id` CHAR(36) NOT NULL COMMENT '부서 ID',
                                `template_type` VARCHAR(100) NOT NULL COMMENT '유형',
                                `deleted_at` DATETIME NOT NULL COMMENT '삭제일',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일',
                                CONSTRAINT `PK_hr_templates` PRIMARY KEY (`hr_template_id`),
                                CONSTRAINT `CK_hr_templates_template_type_1` CHECK (`template_type` IN ('ONBOARDING','OFFBOARDING','DEPARTMENT_TRANSFER','LEAVE','RETURN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `hr_template_items` (
                                     `hr_template_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'HR 템플릿 아이템 ID',
                                     `hr_template_id` CHAR(36) NOT NULL COMMENT 'HR 템플릿 ID',
                                     `intangible_asset_item_id` CHAR(36) NULL DEFAULT NULL COMMENT '무형자산 품목 ID',
                                     `tangible_asset_item_id` CHAR(36) NULL DEFAULT NULL COMMENT '유형자산 품목 ID',
                                     `quantity` INT NULL DEFAULT 1 COMMENT '수량',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일',
                                     CONSTRAINT `PK_hr_template_items` PRIMARY KEY (`hr_template_item_id`),
                                     CONSTRAINT `CK_hr_template_items_XOR_1` CHECK (((`tangible_asset_item_id` IS NOT NULL) + (`intangible_asset_item_id` IS NOT NULL)) = 1),
                                     CONSTRAINT `CK_hr_template_items_quantity_2` CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Foreign key constraints
ALTER TABLE `departments` ADD CONSTRAINT `FK_departments_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `departments` ADD CONSTRAINT `FK_departments_parent_department_id_departments` FOREIGN KEY (`parent_department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `members` ADD CONSTRAINT `FK_members_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `members` ADD CONSTRAINT `FK_members_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tickets` ADD CONSTRAINT `FK_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tickets` ADD CONSTRAINT `FK_tickets_requester_id_members` FOREIGN KEY (`requester_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `tickets` ADD CONSTRAINT `FK_tickets_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `tickets` ADD CONSTRAINT `FK_tickets_approver_id_members` FOREIGN KEY (`approver_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `tickets` ADD CONSTRAINT `FK_tickets_assignee_id_members` FOREIGN KEY (`assignee_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_tangible_asset_item_id_tangible_ass` FOREIGN KEY (`tangible_asset_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_intangible_asset_item_id_intangible` FOREIGN KEY (`intangible_asset_item_id`) REFERENCES `intangible_asset_items` (`intangible_asset_item_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `asset_request_tickets` ADD CONSTRAINT `FK_asset_request_tickets_intangible_asset_id_intangible_asse` FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`);
ALTER TABLE `rental_tickets` ADD CONSTRAINT `FK_rental_tickets_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `rental_tickets` ADD CONSTRAINT `FK_rental_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `rental_tickets` ADD CONSTRAINT `FK_rental_tickets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `maintenance_tickets` ADD CONSTRAINT `FK_maintenance_tickets_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `maintenance_tickets` ADD CONSTRAINT `FK_maintenance_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `maintenance_tickets` ADD CONSTRAINT `FK_maintenance_tickets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `asset_return_tickets` ADD CONSTRAINT `FK_asset_return_tickets_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `asset_return_tickets` ADD CONSTRAINT `FK_asset_return_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `asset_return_tickets` ADD CONSTRAINT `FK_asset_return_tickets_intangible_asset_id_intangible_asset` FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`);
ALTER TABLE `asset_return_tickets` ADD CONSTRAINT `FK_asset_return_tickets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `purchase_return_tickets` ADD CONSTRAINT `FK_purchase_return_tickets_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `purchase_return_tickets` ADD CONSTRAINT `FK_purchase_return_tickets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `purchase_return_tickets` ADD CONSTRAINT `FK_purchase_return_tickets_intangible_asset_id_intangible_as` FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`);
ALTER TABLE `purchase_return_tickets` ADD CONSTRAINT `FK_purchase_return_tickets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `ticket_comments` ADD CONSTRAINT `FK_ticket_comments_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `ticket_comments` ADD CONSTRAINT `FK_ticket_comments_ticket_id_tickets` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`);
ALTER TABLE `ticket_comments` ADD CONSTRAINT `FK_ticket_comments_writer_id_members` FOREIGN KEY (`writer_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `tangible_asset_categories` ADD CONSTRAINT `FK_tangible_asset_categories_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tangible_asset_categories` ADD CONSTRAINT `FK_tangible_asset_categories_parent_id_tangible_asset_catego` FOREIGN KEY (`parent_id`) REFERENCES `tangible_asset_categories` (`tangible_asset_category_id`);
ALTER TABLE `tangible_asset_items` ADD CONSTRAINT `FK_tangible_asset_items_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tangible_asset_items` ADD CONSTRAINT `FK_tangible_asset_items_category_id_tangible_asset_categorie` FOREIGN KEY (`category_id`) REFERENCES `tangible_asset_categories` (`tangible_asset_category_id`);
ALTER TABLE `tangible_assets` ADD CONSTRAINT `FK_tangible_assets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tangible_assets` ADD CONSTRAINT `FK_tangible_assets_tangible_item_id_tangible_asset_items` FOREIGN KEY (`tangible_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`);
ALTER TABLE `tangible_assets` ADD CONSTRAINT `FK_tangible_assets_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `tangible_assets` ADD CONSTRAINT `FK_tangible_assets_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `tangible_asset_assignments` ADD CONSTRAINT `FK_tangible_asset_assignments_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `tangible_asset_assignments` ADD CONSTRAINT `FK_tangible_asset_assignments_tangible_asset_id_tangible_ass` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `tangible_asset_assignments` ADD CONSTRAINT `FK_tangible_asset_assignments_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `tangible_asset_assignments` ADD CONSTRAINT `FK_tangible_asset_assignments_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `intangible_asset_categories` ADD CONSTRAINT `FK_intangible_asset_categories_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `intangible_asset_categories` ADD CONSTRAINT `FK_intangible_asset_categories_parent_id_intangible_asset_ca` FOREIGN KEY (`parent_id`) REFERENCES `intangible_asset_categories` (`intangible_asset_category_id`);
ALTER TABLE `intangible_asset_items` ADD CONSTRAINT `FK_intangible_asset_items_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `intangible_asset_items` ADD CONSTRAINT `FK_intangible_asset_items_category_id_intangible_asset_categ` FOREIGN KEY (`category_id`) REFERENCES `intangible_asset_categories` (`intangible_asset_category_id`);
ALTER TABLE `intangible_assets` ADD CONSTRAINT `FK_intangible_assets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `intangible_assets` ADD CONSTRAINT `FK_intangible_assets_intangible_item_id_intangible_asset_ite` FOREIGN KEY (`intangible_item_id`) REFERENCES `intangible_asset_items` (`intangible_asset_item_id`);
ALTER TABLE `intangible_assets` ADD CONSTRAINT `FK_intangible_assets_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `intangible_assets` ADD CONSTRAINT `FK_intangible_assets_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `intangible_asset_assignments` ADD CONSTRAINT `FK_intangible_asset_assignments_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `intangible_asset_assignments` ADD CONSTRAINT `FK_intangible_asset_assignments_intangible_asset_id_intangib` FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`);
ALTER TABLE `intangible_asset_assignments` ADD CONSTRAINT `FK_intangible_asset_assignments_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `intangible_asset_assignments` ADD CONSTRAINT `FK_intangible_asset_assignments_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `inspections` ADD CONSTRAINT `FK_inspections_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `inspections` ADD CONSTRAINT `FK_inspections_target_department_id_departments` FOREIGN KEY (`target_department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `inspections` ADD CONSTRAINT `FK_inspections_inspector_id_members` FOREIGN KEY (`inspector_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `inspection_targets` ADD CONSTRAINT `FK_inspection_targets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `inspection_targets` ADD CONSTRAINT `FK_inspection_targets_inspection_id_inspections` FOREIGN KEY (`inspection_id`) REFERENCES `inspections` (`inspection_id`);
ALTER TABLE `inspection_targets` ADD CONSTRAINT `FK_inspection_targets_tangible_asset_id_tangible_assets` FOREIGN KEY (`tangible_asset_id`) REFERENCES `tangible_assets` (`tangible_asset_id`);
ALTER TABLE `inspection_targets` ADD CONSTRAINT `FK_inspection_targets_intangible_asset_id_intangible_assets` FOREIGN KEY (`intangible_asset_id`) REFERENCES `intangible_assets` (`intangible_asset_id`);
ALTER TABLE `inspection_results` ADD CONSTRAINT `FK_inspection_results_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `inspection_results` ADD CONSTRAINT `FK_inspection_results_inspection_id_inspections` FOREIGN KEY (`inspection_id`) REFERENCES `inspections` (`inspection_id`);
ALTER TABLE `inspection_results` ADD CONSTRAINT `FK_inspection_results_inspection_target_id_inspection_target` FOREIGN KEY (`inspection_target_id`) REFERENCES `inspection_targets` (`inspection_target_id`);
ALTER TABLE `inspection_results` ADD CONSTRAINT `FK_inspection_results_reviewer_id_members` FOREIGN KEY (`reviewer_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `inspection_follow_ups` ADD CONSTRAINT `FK_inspection_follow_ups_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `inspection_follow_ups` ADD CONSTRAINT `FK_inspection_follow_ups_inspection_result_id_inspection_res` FOREIGN KEY (`inspection_result_id`) REFERENCES `inspection_results` (`inspection_result_id`);
ALTER TABLE `inspection_follow_ups` ADD CONSTRAINT `FK_inspection_follow_ups_processor_id_members` FOREIGN KEY (`processor_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `audit_logs` ADD CONSTRAINT `FK_audit_logs_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `audit_logs` ADD CONSTRAINT `FK_audit_logs_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `activity_logs` ADD CONSTRAINT `FK_activity_logs_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `activity_logs` ADD CONSTRAINT `FK_activity_logs_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `notifications` ADD CONSTRAINT `FK_notifications_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `notifications` ADD CONSTRAINT `FK_notifications_receiver_id_members` FOREIGN KEY (`receiver_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `files` ADD CONSTRAINT `FK_files_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `files` ADD CONSTRAINT `FK_files_uploader_id_members` FOREIGN KEY (`uploader_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `purchase_policies` ADD CONSTRAINT `FK_purchase_policies_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `purchase_requests` ADD CONSTRAINT `FK_purchase_requests_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `purchase_requests` ADD CONSTRAINT `FK_purchase_requests_requester_id_members` FOREIGN KEY (`requester_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `purchase_request_items` ADD CONSTRAINT `FK_purchase_request_items_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `purchase_request_items` ADD CONSTRAINT `FK_purchase_request_items_request_id_purchase_requests` FOREIGN KEY (`request_id`) REFERENCES `purchase_requests` (`request_id`);
ALTER TABLE `purchase_request_items` ADD CONSTRAINT `FK_purchase_request_items_intangible_asset_item_id_intangibl` FOREIGN KEY (`intangible_asset_item_id`) REFERENCES `intangible_asset_items` (`intangible_asset_item_id`);
ALTER TABLE `purchase_request_items` ADD CONSTRAINT `FK_purchase_request_items_tangible_asset_item_id_tangible_as` FOREIGN KEY (`tangible_asset_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`);
ALTER TABLE `purchase_request_items` ADD CONSTRAINT `FK_purchase_request_items_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `purchase_evidences` ADD CONSTRAINT `FK_purchase_evidences_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `purchase_evidences` ADD CONSTRAINT `FK_purchase_evidences_request_id_purchase_requests` FOREIGN KEY (`request_id`) REFERENCES `purchase_requests` (`request_id`);
ALTER TABLE `purchase_evidences` ADD CONSTRAINT `FK_purchase_evidences_intangible_asset_item_id_intangible_as` FOREIGN KEY (`intangible_asset_item_id`) REFERENCES `intangible_asset_items` (`intangible_asset_item_id`);
ALTER TABLE `purchase_evidences` ADD CONSTRAINT `FK_purchase_evidences_tangible_asset_item_id_tangible_asset_` FOREIGN KEY (`tangible_asset_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`);
ALTER TABLE `purchase_evidences` ADD CONSTRAINT `FK_purchase_evidences_file_id_files` FOREIGN KEY (`file_id`) REFERENCES `files` (`file_id`);
ALTER TABLE `budgets` ADD CONSTRAINT `FK_budgets_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `budgets` ADD CONSTRAINT `FK_budgets_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `budget_histories` ADD CONSTRAINT `FK_budget_histories_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `budget_histories` ADD CONSTRAINT `FK_budget_histories_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `budget_histories` ADD CONSTRAINT `FK_budget_histories_budget_id_budgets` FOREIGN KEY (`budget_id`) REFERENCES `budgets` (`budget_id`);
ALTER TABLE `hr_events` ADD CONSTRAINT `FK_hr_events_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `hr_events` ADD CONSTRAINT `FK_hr_events_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `hr_events` ADD CONSTRAINT `FK_hr_events_member_id_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`);
ALTER TABLE `hr_templates` ADD CONSTRAINT `FK_hr_templates_company_id_companies` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`);
ALTER TABLE `hr_templates` ADD CONSTRAINT `FK_hr_templates_department_id_departments` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);
ALTER TABLE `hr_template_items` ADD CONSTRAINT `FK_hr_template_items_hr_template_id_hr_templates` FOREIGN KEY (`hr_template_id`) REFERENCES `hr_templates` (`hr_template_id`);
ALTER TABLE `hr_template_items` ADD CONSTRAINT `FK_hr_template_items_intangible_asset_item_id_intangible_ass` FOREIGN KEY (`intangible_asset_item_id`) REFERENCES `intangible_asset_items` (`intangible_asset_item_id`);
ALTER TABLE `hr_template_items` ADD CONSTRAINT `FK_hr_template_items_tangible_asset_item_id_tangible_asset_i` FOREIGN KEY (`tangible_asset_item_id`) REFERENCES `tangible_asset_items` (`tangible_asset_item_id`);
