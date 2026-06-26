package com.ieumsae.assetieum.domain.intangibleasset.category.repository;

import java.util.List;
import java.util.UUID;

public interface IntangibleAssetCategoryRepositoryCustom {
    List<UUID> findAllDescendantIds(UUID categoryId, UUID companyId);
}
