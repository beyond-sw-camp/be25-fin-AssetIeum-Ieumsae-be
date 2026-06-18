ALTER TABLE `tangible_asset_categories`
    ADD CONSTRAINT `UK_tangible_asset_categories_company_id_name`
        UNIQUE (`company_id`, `name`);

ALTER TABLE `intangible_asset_categories`
    ADD CONSTRAINT `UK_intangible_asset_categories_company_id_name`
        UNIQUE (`company_id`, `name`);
