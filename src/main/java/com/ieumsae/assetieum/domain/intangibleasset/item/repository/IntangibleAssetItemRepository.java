package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntangibleAssetItemRepository extends JpaRepository<IntangibleAssetItem, UUID> {

	Optional<IntangibleAssetItem> findByIdAndDeletedAtIsNull(UUID itemId);
}
