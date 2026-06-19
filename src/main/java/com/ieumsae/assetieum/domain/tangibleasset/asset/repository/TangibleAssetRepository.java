package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

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

    boolean existsByCompany_IdAndSerialNumber(UUID companyId, String serialNumber);

    boolean existsByAssetCode(String assetCode);

    Optional<TangibleAsset> findByIdAndCompany_Id(UUID assetId, UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TangibleAsset> findWithLockByIdAndCompany_Id(UUID assetId, UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select asset
            from TangibleAsset asset
            where asset.company.id = :companyId
              and asset.tangibleAssetItem.id = :itemId
              and asset.tangibleAssetStatus = :status
            order by asset.createdAt asc
            """)
    List<TangibleAsset> findAvailableAssetsWithLock(UUID companyId, UUID itemId, TangibleAssetStatus status, Pageable pageable);

    @Query("""
            select asset.purchasePrice
            from TangibleAsset asset
            where asset.company.id = :companyId
              and asset.tangibleAssetItem.id = :itemId
              and asset.purchasePrice is not null
            order by asset.purchaseDate desc, asset.createdAt desc
            """)
    List<BigDecimal> findRecentPurchasePrices(UUID companyId, UUID itemId, Pageable pageable);

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
