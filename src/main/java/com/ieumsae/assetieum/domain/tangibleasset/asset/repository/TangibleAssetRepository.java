package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    // 구매계획 후보 조회에서 예상단가가 없을 때 최근 구매금액을 사용한다.
    @Query("""
            select asset.purchasePrice
            from TangibleAsset asset
            where asset.company.id = :companyId
              and asset.tangibleAssetItem.id = :itemId
              and asset.purchasePrice is not null
            order by asset.purchaseDate desc, asset.createdAt desc
            """)
    List<BigDecimal> findRecentPurchasePrices(UUID companyId, UUID itemId, Pageable pageable);

    // 대여 티켓에서 할당 가능한 유형자산 목록을 조회한다.
    @Query("""
            select asset
            from TangibleAsset asset
            where asset.company.id = :companyId
              and asset.tangibleAssetItem.id = :itemId
              and asset.tangibleAssetStatus = :status
              and (
                    :keyword is null
                    or lower(asset.assetCode) like lower(concat('%', :keyword, '%'))
                    or lower(asset.serialNumber) like lower(concat('%', :keyword, '%'))
                    or lower(asset.location) like lower(concat('%', :keyword, '%'))
                  )
            order by asset.createdAt asc
            """)
    Page<TangibleAsset> searchRentalAssignableAssets(
            UUID companyId,
            UUID itemId,
            TangibleAssetStatus status,
            String keyword,
            Pageable pageable
    );

    List<TangibleAsset> findAllByCompany_IdAndTangibleAssetStatus(UUID companyId, TangibleAssetStatus status);

    List<TangibleAsset> findAllByCompany_IdAndDepartment_IdInAndTangibleAssetStatus(
            UUID companyId,
            List<UUID> departmentIds,
            TangibleAssetStatus status
    );

    List<TangibleAsset> findAllByCompany_IdAndTangibleAssetItem_TangibleAssetCategory_IdInAndTangibleAssetStatus(
            UUID companyId,
            List<UUID> categoryIds,
            TangibleAssetStatus status
    );

    Optional<TangibleAsset> findByIdAndCompany_IdAndTangibleAssetStatus(UUID assetId, UUID companyId, TangibleAssetStatus tangibleAssetStatus);
}
