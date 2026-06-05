package com.ieumsae.assetieum.domain.tangibleasset.category.dto;

import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TangibleAssetCategoryTreeResponse {
    private UUID categoryId;

    private UUID companyId;

    private UUID parentId;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    private List<TangibleAssetCategoryTreeResponse> children = new ArrayList<>();

    public static TangibleAssetCategoryTreeResponse from(
            TangibleAssetCategory category
    ) {
        return TangibleAssetCategoryTreeResponse.builder()
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

    public void addChild(TangibleAssetCategoryTreeResponse child) {
        this.children.add(child);
    }
}
