package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntangibleAssetRepository extends JpaRepository<IntangibleAsset, UUID> {

    boolean existsByCompany_IdAndIntangibleAssetItem_Id(UUID id, UUID id1);
}
