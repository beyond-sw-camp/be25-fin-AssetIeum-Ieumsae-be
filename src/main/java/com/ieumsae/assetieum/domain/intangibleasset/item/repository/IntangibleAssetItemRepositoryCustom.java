package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IntangibleAssetItemRepositoryCustom {
    Page<IntangibleAssetItem> search(
            UUID companyId,
            UUID categoryId,
            String keyword,
            Boolean isStandard,
            Pageable pageable
    );
}
