package com.ieumsae.assetieum.domain.tangibleasset.assignment.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetAssignmentRequest {
    @NotNull
    private UUID memberId;

    @NotNull
    private UsageType usageType;

    @NotNull
    private AssetUsageType assetUsageType;

    private LocalDateTime endedAt;
}
