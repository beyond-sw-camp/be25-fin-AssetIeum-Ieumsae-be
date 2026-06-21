-- V37: direct_purchase_results 테이블에 증빙 파일 및 확인 상태 컬럼 추가

ALTER TABLE direct_purchase_results
    ADD COLUMN proof_file_url          VARCHAR(500)  NULL        COMMENT '증빙 파일 URL',
    ADD COLUMN proof_file_uploaded_at  DATETIME      NULL        COMMENT '증빙 파일 업로드 일시',
    ADD COLUMN confirmation_status     VARCHAR(30)   NOT NULL DEFAULT 'PENDING' COMMENT '확인 상태 (PENDING: 미확인, CONFIRMED: 확인완료)';
