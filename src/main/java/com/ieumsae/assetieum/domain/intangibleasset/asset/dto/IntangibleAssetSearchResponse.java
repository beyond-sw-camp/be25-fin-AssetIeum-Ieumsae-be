package com.ieumsae.assetieum.domain.intangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetSearchResponse {

    private String productName;

    private String assetCode;

    private String currentUserName;

    private String currentUserMemberNo;

    private IntangibleAssetStatus intangibleAssetStatus;

    private String departmentName;

}
