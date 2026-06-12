package com.ieumsae.assetieum.domain.intangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetUpdateRequest {

    private IntangibleAssetStatus intangibleAssetStatus;

    private Integer seatCount;

    private Boolean isAutoRenewal;

    private LocalDateTime startedAt;

    private LocalDateTime expiredAt;

    private UUID memberId;

    private UUID departmentId;
}
