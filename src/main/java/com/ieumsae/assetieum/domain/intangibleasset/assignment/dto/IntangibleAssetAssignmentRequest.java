package com.ieumsae.assetieum.domain.intangibleasset.assignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetAssignmentRequest {

    @NotNull()
    private UUID memberId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;

    public static IntangibleAssetAssignmentRequest of(UUID memberId, LocalDateTime endedAt) {
        return new IntangibleAssetAssignmentRequest(memberId, endedAt);
    }
}
