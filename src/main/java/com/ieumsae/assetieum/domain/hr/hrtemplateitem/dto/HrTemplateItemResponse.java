package com.ieumsae.assetieum.domain.hr.hrtemplateitem.dto;

import com.ieumsae.assetieum.domain.hr.hrtemplateitem.entity.HrTemplateItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HrTemplateItemResponse {

    private Long hrTemplateItemId;

    private AssetType assetType;

    private UUID assetItemId;

    private String productName;

    private Integer quantity;

    public static HrTemplateItemResponse from(HrTemplateItem item) {
        TangibleAssetItem tangibleAssetItem = item.getTangibleAssetItem();
        IntangibleAssetItem intangibleAssetItem = item.getIntangibleAssetItem();

        if (tangibleAssetItem != null) {
            return HrTemplateItemResponse.builder()
                    .hrTemplateItemId(item.getId())
                    .assetType(AssetType.TANGIBLE)
                    .assetItemId(tangibleAssetItem.getId())
                    .productName(tangibleAssetItem.getProductName())
                    .quantity(item.getQuantity())
                    .build();
        }

        return HrTemplateItemResponse.builder()
                .hrTemplateItemId(item.getId())
                .assetType(AssetType.INTANGIBLE)
                .assetItemId(intangibleAssetItem.getId())
                .productName(intangibleAssetItem.getProductName())
                .quantity(item.getQuantity())
                .build();
    }
}
