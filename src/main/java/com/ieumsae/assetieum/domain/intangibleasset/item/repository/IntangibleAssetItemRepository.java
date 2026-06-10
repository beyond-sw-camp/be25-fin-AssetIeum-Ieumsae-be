package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntangibleAssetItemRepository extends JpaRepository<IntangibleAssetItem, UUID>, IntangibleAssetItemRepositoryCustom{

    Optional<IntangibleAssetItem> findByIdAndDeletedAtIsNull(UUID itemId);

    boolean existsByCompany_IdAndProductName(UUID id, String productName);
}
