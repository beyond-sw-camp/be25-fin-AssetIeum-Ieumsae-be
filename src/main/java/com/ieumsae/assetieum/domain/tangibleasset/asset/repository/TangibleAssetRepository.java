package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetRepository extends JpaRepository<TangibleAsset, UUID>, TangibleAssetRepositoryCustom {
    boolean existsByCompany_IdAndTangibleAssetItem_Id(UUID id, UUID id1);

    boolean existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(UUID company_id, String serialNumber, UUID tangibleAssetItem_id);

    boolean existsByAssetCode(String assetCode);

    Optional<TangibleAsset> findByIdAndCompany_Id(UUID assetId, UUID companyId);
}
