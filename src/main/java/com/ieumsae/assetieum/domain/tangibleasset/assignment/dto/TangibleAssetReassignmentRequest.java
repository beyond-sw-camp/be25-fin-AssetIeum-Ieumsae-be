package com.ieumsae.assetieum.domain.tangibleasset.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetReassignmentRequest {
    @NotNull
    private UUID newMemberId;
}
