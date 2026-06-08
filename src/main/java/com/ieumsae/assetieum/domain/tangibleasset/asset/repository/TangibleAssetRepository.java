package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TangibleAssetRepository extends JpaRepository<TangibleAsset, UUID> {
    boolean existsByCompany_IdAndTangibleAssetItem_Id(UUID id, UUID id1);
}
