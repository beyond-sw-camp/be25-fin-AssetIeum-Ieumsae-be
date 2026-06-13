package com.ieumsae.assetieum.domain.intangibleasset.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class IntangibleAssetAssignmentRequest {

    @NotNull()
    private UUID memberId;

    private LocalDateTime endedAt;
}
