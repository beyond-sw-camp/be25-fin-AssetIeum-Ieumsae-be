ALTER TABLE purchase_plans
    ADD CONSTRAINT UK_purchase_plans_plan_no UNIQUE (plan_no);

ALTER TABLE purchase_plans
    ADD CONSTRAINT CK_purchase_plans_purchase_request_status_1
        CHECK (purchase_request_status IN (
            'REQUESTED',
            'APPROVED',
            'REJECTED',
            'ORDERED',
            'DELIVERED',
            'COMPLETED',
            'CANCELLED'
        ));

ALTER TABLE purchase_plan_items
    ADD COLUMN ticket_id CHAR(36) NULL COMMENT '구매 요청 티켓 ID'
        AFTER plan_id;

ALTER TABLE purchase_plan_items
    MODIFY COLUMN department_id CHAR(36) NULL COMMENT '부서 ID - 비용을 청구할 대상 부서';

ALTER TABLE purchase_plan_items
    ADD CONSTRAINT FK_purchase_plan_items_ticket_id_purchase_request_tickets
        FOREIGN KEY (ticket_id)
        REFERENCES purchase_request_tickets (ticket_id);
