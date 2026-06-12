package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntangibleAssetRepository extends JpaRepository<IntangibleAsset, UUID> {

    boolean existsByCompany_IdAndIntangibleAssetItem_Id(UUID id, UUID id1);

    boolean existsByCompany_IdAndLicenseCodeAndIntangibleAssetItem_Id(UUID companyId, String licenseCode, @NotNull UUID intangibleItemId);
}
