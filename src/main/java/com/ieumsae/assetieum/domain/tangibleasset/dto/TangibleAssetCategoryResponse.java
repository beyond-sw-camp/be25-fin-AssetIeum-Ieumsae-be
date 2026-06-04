package com.ieumsae.assetieum.domain.tangibleasset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.entity.TangibleAssetCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TangibleAssetCategoryResponse {
    private UUID categoryId;

    private UUID companyId;

    private UUID parentId;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static TangibleAssetCategoryResponse from(
            TangibleAssetCategory category
    ) {
        return TangibleAssetCategoryResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .parentId(
                        category.getParent() != null
                                ? category.getParent().getId()
                                : null
                )
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
