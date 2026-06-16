package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntangibleAssetRepository extends JpaRepository<IntangibleAsset, UUID>, IntangibleAssetRepositoryCustom {

    boolean existsByCompany_IdAndIntangibleAssetItem_Id(UUID id, UUID id1);

    long countByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatus(
            UUID companyId,
            UUID intangibleAssetItemId,
            IntangibleAssetStatus status
    );

    List<IntangibleAsset> findAllByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatusIn(
            UUID companyId,
            UUID intangibleAssetItemId,
            List<IntangibleAssetStatus> statuses
    );

    boolean existsByCompany_IdAndLicenseCodeAndIntangibleAssetItem_Id(UUID companyId, String licenseCode, @NotNull UUID intangibleItemId);

    Optional<IntangibleAsset> findByIdAndCompany_Id(UUID assetId, UUID companyId);

    List<IntangibleAsset> findAllByCompany_IdAndIntangibleAssetStatus(UUID companyId, IntangibleAssetStatus status);

    List<IntangibleAsset> findAllByCompany_IdAndDepartment_IdAndIntangibleAssetStatus(
            UUID companyId,
            UUID departmentId,
            IntangibleAssetStatus status
    );

    List<IntangibleAsset> findAllByCompany_IdAndIntangibleAssetItem_IntangibleAssetCategory_IdInAndIntangibleAssetStatus(
            UUID companyId,
            List<UUID> categoryIds,
            IntangibleAssetStatus status
    );
}
