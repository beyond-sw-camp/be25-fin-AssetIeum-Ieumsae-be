package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "companyId",
        "tangibleItemId",
        "serialNumber",
        "location",
        "purchaseDate",
        "purchasePrice",
        "purchaseVendor",
        "warrantyExpiredAt",
})
public class TangibleAssetResponse {

    private UUID tangibleAssetId;
    private UUID companyId;
    private UUID tangibleItemId;

    private String assetCode;
    private String serialNumber;

    private UsageType usageType;
    private AssetUsageType assetUsageType;
    private TangibleAssetStatus tangibleAssetStatus;

    private UUID memberId;
    private UUID departmentId;

    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime usedStartedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnDueDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseVendor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime warrantyExpiredAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static TangibleAssetResponse from(TangibleAsset tangibleAsset) {
        return TangibleAssetResponse.builder()
                .tangibleAssetId(tangibleAsset.getId())
                .companyId(tangibleAsset.getCompany().getId())
                .tangibleItemId(tangibleAsset.getTangibleAssetItem().getId())
                .assetCode(tangibleAsset.getAssetCode())
                .serialNumber(tangibleAsset.getSerialNumber())
                .usageType(tangibleAsset.getUsageType())
                .assetUsageType(tangibleAsset.getAssetUsageType())
                .tangibleAssetStatus(tangibleAsset.getTangibleAssetStatus())
                .memberId(
                        tangibleAsset.getMember() != null
                                ? tangibleAsset.getMember().getId()
                                : null
                )
                .departmentId(
                        tangibleAsset.getDepartment() != null
                                ? tangibleAsset.getDepartment().getId()
                                : null
                )
                .location(tangibleAsset.getLocation())
                .usedStartedAt(tangibleAsset.getUsedStartedAt())
                .returnDueDate(tangibleAsset.getReturnDueDate())
                .purchaseDate(tangibleAsset.getPurchaseDate())
                .purchasePrice(tangibleAsset.getPurchasePrice())
                .purchaseVendor(tangibleAsset.getPurchaseVendor())
                .warrantyExpiredAt(tangibleAsset.getWarrantyExpiredAt())
                .createdAt(tangibleAsset.getCreatedAt())
                .updatedAt(tangibleAsset.getUpdatedAt())
                .build();
    }
}