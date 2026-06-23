package com.ieumsae.assetieum.domain.hr.hrevent.dto;

import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.dto.HrEventAssetTargetCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HrEventCreateRequest {
    @NotNull
    private UUID memberId;

    private UUID targetDepartmentId;

    @NotNull
    private HrEventType eventType;

    @NotNull
    private LocalDateTime eventDate;

    @Valid
    private List<HrEventAssetTargetCreateRequest> assetTargets;
}
