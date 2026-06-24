CREATE TABLE ticket_assignment_targets (
    target_id CHAR(36) NOT NULL COMMENT 'Assignment target ID',
    ticket_id CHAR(36) NOT NULL COMMENT 'Ticket ID',
    company_id CHAR(36) NOT NULL COMMENT 'Company ID',
    member_id CHAR(36) NOT NULL COMMENT 'Target member ID',
    target_status VARCHAR(30) NOT NULL COMMENT 'Target assignment status',
    assigned_asset_type VARCHAR(30) NULL COMMENT 'Assigned asset type',
    assigned_asset_id CHAR(36) NULL COMMENT 'Assigned asset ID',
    assigned_at DATETIME(6) NULL COMMENT 'Assigned at',
    created_at DATETIME(6) NOT NULL COMMENT 'Created at',
    updated_at DATETIME(6) NOT NULL COMMENT 'Updated at',
    PRIMARY KEY (target_id),
    CONSTRAINT fk_ticket_assignment_targets_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id),
    CONSTRAINT fk_ticket_assignment_targets_company
        FOREIGN KEY (company_id) REFERENCES companies(company_id),
    CONSTRAINT fk_ticket_assignment_targets_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    INDEX idx_ticket_assignment_targets_ticket (ticket_id),
    INDEX idx_ticket_assignment_targets_company_member (company_id, member_id),
    UNIQUE KEY uk_ticket_assignment_targets_ticket_member (ticket_id, member_id)
) COMMENT = 'Ticket assignment targets';
