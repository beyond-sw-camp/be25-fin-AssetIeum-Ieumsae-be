package com.ieumsae.assetieum.domain.intangibleasset.category.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "categoryId",
        "parentId",
        "name",
        "createdAt",
        "updatedAt",
        "children"
})
public class IntangibleAssetCategoryTreeResponse {
    private UUID categoryId;

    private UUID parentId;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    private List<IntangibleAssetCategoryTreeResponse> children = new ArrayList<>();

    public static IntangibleAssetCategoryTreeResponse from(
            IntangibleAssetCategory category
    ) {
        return IntangibleAssetCategoryTreeResponse.builder()
                .categoryId(category.getId())
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

    public void addChild(IntangibleAssetCategoryTreeResponse child) {
        this.children.add(child);
    }

}
