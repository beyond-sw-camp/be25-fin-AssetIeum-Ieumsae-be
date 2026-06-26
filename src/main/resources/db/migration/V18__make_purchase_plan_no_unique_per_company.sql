ALTER TABLE purchase_plans
    DROP INDEX UK_purchase_plans_plan_no;

ALTER TABLE purchase_plans
    ADD CONSTRAINT UK_purchase_plans_company_id_plan_no UNIQUE (company_id, plan_no);

ALTER TABLE tickets
    DROP INDEX UK_tickets_ticket_no;

ALTER TABLE tickets
    ADD CONSTRAINT UK_tickets_company_id_ticket_no UNIQUE (company_id, ticket_no);

ALTER TABLE tangible_assets
    DROP INDEX UK_tangible_assets_asset_code;

ALTER TABLE tangible_assets
    ADD CONSTRAINT UK_tangible_assets_company_id_asset_code UNIQUE (company_id, asset_code);

ALTER TABLE intangible_assets
    DROP INDEX UK_intangible_assets_asset_code;

ALTER TABLE intangible_assets
    ADD CONSTRAINT UK_intangible_assets_company_id_asset_code UNIQUE (company_id, asset_code);
