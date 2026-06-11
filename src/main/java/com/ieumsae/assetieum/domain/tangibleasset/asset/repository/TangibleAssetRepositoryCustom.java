package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetRepositoryCustom {
    Page<TangibleAssetSearchResponse> search(
            UUID companyId,
            UUID categoryId,
            TangibleAssetStatus status,
            String keyword,
            UUID currentUserId,
            UUID departmentId,
            Pageable pageable
    );

    Optional<TangibleAssetDetailResponse> findDetailByIdAndCompanyId(UUID assetId, UUID companyId);

}
