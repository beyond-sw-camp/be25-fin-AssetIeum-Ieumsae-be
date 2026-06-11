package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetUpdateRequest {

    private TangibleAssetStatus tangibleAssetStatus;

    private UsageType usageType;

    private String location;

    private LocalDateTime usedStartedAt;

    private LocalDateTime returnDueDate;

    private UUID departmentId;

    private UUID memberId;

}