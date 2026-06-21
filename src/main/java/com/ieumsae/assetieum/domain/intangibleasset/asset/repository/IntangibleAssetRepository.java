package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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

    boolean existsByCompany_IdAndLicenseCode(UUID companyId, String licenseCode);

    Optional<IntangibleAsset> findByIdAndCompany_Id(UUID assetId, UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IntangibleAsset> findWithLockByIdAndCompany_Id(UUID assetId, UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select asset
            from IntangibleAsset asset
            where asset.company.id = :companyId
              and asset.intangibleAssetItem.id = :itemId
              and asset.intangibleAssetStatus in :statuses
            order by asset.createdAt asc
            """)
    List<IntangibleAsset> findAssignableAssetsWithLock(
            UUID companyId,
            UUID itemId,
            List<IntangibleAssetStatus> statuses,
            Pageable pageable
    );

    @Query("""
            select asset.purchasePrice
            from IntangibleAsset asset
            where asset.company.id = :companyId
              and asset.intangibleAssetItem.id = :itemId
              and asset.purchasePrice is not null
            order by asset.purchaseDate desc, asset.createdAt desc
            """)
    List<BigDecimal> findRecentPurchasePrices(UUID companyId, UUID itemId, Pageable pageable);

    List<IntangibleAsset> findAllByCompany_IdAndIntangibleAssetStatus(UUID companyId, IntangibleAssetStatus status);

    List<IntangibleAsset> findAllByCompany_IdAndDepartment_IdInAndIntangibleAssetStatus(
            UUID companyId,
            List<UUID> departmentIds,
            IntangibleAssetStatus status
    );

    List<IntangibleAsset> findAllByCompany_IdAndIntangibleAssetItem_IntangibleAssetCategory_IdInAndIntangibleAssetStatus(
            UUID companyId,
            List<UUID> categoryIds,
            IntangibleAssetStatus status
    );
}
