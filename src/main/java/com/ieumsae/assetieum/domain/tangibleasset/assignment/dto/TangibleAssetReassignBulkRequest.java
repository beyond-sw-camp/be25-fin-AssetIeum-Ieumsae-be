package com.ieumsae.assetieum.domain.tangibleasset.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetReassignBulkRequest {
    @NotNull
    private UUID currentMemberId;

    @NotNull
    private UUID newMemberId;
}
