ALTER TABLE `intangible_asset_items`
    ADD CONSTRAINT `CK_intangible_asset_items_license_type`
        CHECK (
            `license_type` IN (
                               'SUBSCRIPTION',
                               'PERPETUAL',
                               'TERM'
                )
            );