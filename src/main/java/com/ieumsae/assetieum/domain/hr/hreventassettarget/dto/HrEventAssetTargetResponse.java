package com.ieumsae.assetieum.domain.hr.hreventassettarget.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.type.HrEventAssetActionType;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.type.HrEventAssetTargetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonPropertyOrder({
        "hrEventAssetTargetId",
        "hrEventId",
        "memberId",
        "memberName",
        "transferMemberId",
        "transferMemberName",
        "assetType",
        "assetId",
        "assetCode",
        "productName",
        "actionType",
        "targetStatus",
        "processedAt",
        "createdAt",
        "updatedAt"
})
public class HrEventAssetTargetResponse {

    private UUID hrEventAssetTargetId;

    private UUID hrEventId;

    private UUID memberId;

    private String memberName;

    private UUID transferMemberId;

    private String transferMemberName;

    private AssetType assetType;

    private UUID assetId;

    private String assetCode;

    private String productName;

    private HrEventAssetActionType actionType;

    private HrEventAssetTargetStatus targetStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static HrEventAssetTargetResponse from(HrEventAssetTarget target) {
        return HrEventAssetTargetResponse.builder()
                .hrEventAssetTargetId(target.getId())
                .hrEventId(target.getHrEvent().getId())
                .memberId(target.getMember().getId())
                .memberName(target.getMember().getName())
                .transferMemberId(resolveTransferMemberId(target))
                .transferMemberName(resolveTransferMemberName(target))
                .assetType(target.getAssetType())
                .assetId(resolveAssetId(target))
                .assetCode(resolveAssetCode(target))
                .productName(resolveProductName(target))
                .actionType(target.getActionType())
                .targetStatus(target.getTargetStatus())
                .processedAt(target.getProcessedAt())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .build();
    }

    private static UUID resolveTransferMemberId(HrEventAssetTarget target) {
        if (target.getTransferMember() == null) {
            return null;
        }
        return target.getTransferMember().getId();
    }

    private static String resolveTransferMemberName(HrEventAssetTarget target) {
        if (target.getTransferMember() == null) {
            return null;
        }
        return target.getTransferMember().getName();
    }

    private static UUID resolveAssetId(HrEventAssetTarget target) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            return target.getTangibleAsset().getId();
        }
        return target.getIntangibleAsset().getId();
    }

    private static String resolveAssetCode(HrEventAssetTarget target) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            return target.getTangibleAsset().getAssetCode();
        }
        return target.getIntangibleAsset().getAssetCode();
    }

    private static String resolveProductName(HrEventAssetTarget target) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            TangibleAsset asset = target.getTangibleAsset();
            return asset.getTangibleAssetItem().getProductName();
        }

        IntangibleAsset asset = target.getIntangibleAsset();
        return asset.getIntangibleAssetItem().getProductName();
    }
}
