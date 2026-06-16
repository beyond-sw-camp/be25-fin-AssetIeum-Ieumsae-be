package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TangibleAssetRepository extends JpaRepository<TangibleAsset, UUID>, TangibleAssetRepositoryCustom {

    boolean existsByCompany_IdAndTangibleAssetItem_Id(UUID id, UUID id1);

    boolean existsByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
            UUID companyId,
            UUID tangibleAssetItemId,
            TangibleAssetStatus status
    );

    long countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
            UUID companyId,
            UUID tangibleAssetItemId,
            TangibleAssetStatus status
    );

    boolean existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(UUID company_id, String serialNumber, UUID tangibleAssetItem_id);

    boolean existsByAssetCode(String assetCode);

    Optional<TangibleAsset> findByIdAndCompany_Id(UUID assetId, UUID companyId);

    List<TangibleAsset> findAllByCompany_IdAndTangibleAssetStatus(UUID companyId, TangibleAssetStatus status);

    List<TangibleAsset> findAllByCompany_IdAndDepartment_IdAndTangibleAssetStatus(
            UUID companyId,
            UUID departmentId,
            TangibleAssetStatus status
    );

    List<TangibleAsset> findAllByCompany_IdAndTangibleAssetItem_TangibleAssetCategory_IdInAndTangibleAssetStatus(
            UUID companyId,
            List<UUID> categoryIds,
            TangibleAssetStatus status
    );
}
