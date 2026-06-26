package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetSearchResponse {

    private UUID tangibleAssetId;

    private String productName;

    private String assetCode;

    private String currentUserName;

    private String currentUserMemberNo;

    private TangibleAssetStatus tangibleAssetStatus;

    private String departmentName;
}
