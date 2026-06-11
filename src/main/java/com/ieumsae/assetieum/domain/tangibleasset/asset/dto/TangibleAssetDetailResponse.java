package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "productName",
        "assetCode",
        "serialNumber",
        "status",
        "assetUsageType",
        "usageType",
        "location",
        "usedStartedAt",
        "returnDueDate",
        "departmentName",
        "userName",
        "purchaseDate",
        "purchasePrice",
        "purchaseVendor",
        "warrantyExpiredAt"
})
public class TangibleAssetDetailResponse {
    private String productName;

    private String assetCode;

    private String serialNumber;

    private TangibleAssetStatus status;

    private AssetUsageType assetUsageType;

    private UsageType usageType;

    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime usedStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnDueDate;

    private String departmentName;

    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;

    private BigDecimal purchasePrice;

    private String purchaseVendor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime warrantyExpiredAt;
}
