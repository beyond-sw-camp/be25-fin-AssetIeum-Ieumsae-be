package com.ieumsae.assetieum.domain.hr.hreventassettarget.dto;

import com.ieumsae.assetieum.domain.hr.hreventassettarget.type.HrEventAssetActionType;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HrEventAssetTargetCreateRequest {

    @NotNull
    private AssetType assetType;

    @NotNull
    private UUID assetId;

    @NotNull
    private HrEventAssetActionType actionType;

}
