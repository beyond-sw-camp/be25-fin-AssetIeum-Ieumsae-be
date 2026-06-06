package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TangibleAssetItemRepository extends JpaRepository<TangibleAssetItem, UUID> {
    boolean existsByCompany_IdAndProductName(UUID companyId, @NotBlank String productName);

    boolean existsByCompany_IdAndModelName(UUID companyId, String modelName);
}
