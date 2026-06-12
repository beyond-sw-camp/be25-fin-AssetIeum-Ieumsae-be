package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IntangibleAssetRepositoryCustom {
    Page<IntangibleAssetSearchResponse> search(
            UUID companyId,
            UUID categoryId,
            IntangibleAssetStatus status,
            String keyword,
            UUID currentUserId,
            UUID departmentId,
            Pageable pageable
    );

    Optional<IntangibleAssetDetailResponse> findDetailByIdAndCompanyId(UUID assetId, UUID companyId);
}
