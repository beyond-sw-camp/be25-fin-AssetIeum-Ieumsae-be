ALTER TABLE inspection_targets
    ADD COLUMN member_id CHAR(36) NULL AFTER intangible_asset_id,
    ADD CONSTRAINT fk_inspection_targets_member
        FOREIGN KEY (member_id) REFERENCES members (member_id);

CREATE INDEX idx_inspection_targets_member_id
    ON inspection_targets (member_id);
