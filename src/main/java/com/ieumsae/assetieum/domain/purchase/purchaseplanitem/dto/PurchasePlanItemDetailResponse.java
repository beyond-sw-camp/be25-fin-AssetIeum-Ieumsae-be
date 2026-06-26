package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.file.dto.FileResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "itemId",
    "assetType",
    "status",
    "isStandard",
    "categoryId",
    "categoryName",
    "productName",
    "quantity",
    "estimatedUnitPrice",
    "totalAmount",
    "ticket",
    "evidenceFiles"
})
public class PurchasePlanItemDetailResponse {

    private Long itemId;

    private AssetType assetType;

    private PurchasePlanItemStatus status;

    private Boolean isStandard;

    private UUID categoryId;

    private String categoryName;

    private String productName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    private TicketInfo ticket;

    private List<FileResponse> evidenceFiles;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonPropertyOrder({
            "ticketRequesterId",
            "ticketRequesterName",
            "ticketDepartmentId",
            "ticketDepartmentName",
            "ticketTargetMemberIds"
    })
    public static class TicketInfo {
        private UUID ticketRequesterId;
        private String ticketRequesterName;
        private UUID ticketDepartmentId;
        private String ticketDepartmentName;
        private List<UUID> ticketTargetMemberIds;
    }

    public static PurchasePlanItemDetailResponse from(PurchasePlanItem item, String categoryName) {
        return from(item, categoryName, List.of(), List.of());
    }
    public static PurchasePlanItemDetailResponse from(
            PurchasePlanItem item,
            String categoryName,
            List<UUID> ticketTargetMemberIds,
            List<FileResponse> evidenceFiles
    ) {
        return PurchasePlanItemDetailResponse.builder()
                .itemId(item.getId())
                .assetType(item.getAssetType())
                .status(item.getPurchasePlanItemStatus())
                .isStandard(item.getIsStandard())
                .categoryId(item.getCategoryId())
                .categoryName(categoryName)
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .estimatedUnitPrice(item.getEstimatedUnitPrice())
                .totalAmount(item.getEstimatedUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .ticket(buildTicketInfo(item, ticketTargetMemberIds))
                .evidenceFiles(evidenceFiles)
                .build();
    }

    private static TicketInfo buildTicketInfo(PurchasePlanItem item, java.util.List<UUID> ticketTargetMemberIds) {
        if (item.getTicket() == null) {
            return null;
        }

        return TicketInfo.builder()
                .ticketRequesterId(item.getTicket().getRequester().getId())
                .ticketRequesterName(item.getTicket().getRequester().getName())
                .ticketDepartmentId(item.getTicket().getDepartment().getId())
                .ticketDepartmentName(item.getTicket().getDepartment().getName())
                .ticketTargetMemberIds(ticketTargetMemberIds)
                .build();
    }
}
