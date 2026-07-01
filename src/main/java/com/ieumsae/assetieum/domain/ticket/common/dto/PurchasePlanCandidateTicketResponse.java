package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "ticketId",
        "ticketNo",
        "ticketType",
        "assetType",
        "assetItemId",
        "requesterId",
        "requesterName",
        "departmentId",
        "departmentName",
        "itemName",
        "categoryName",
        "isStandard",
        "quantity",
        "estimatedUnitPrice"
})
public class PurchasePlanCandidateTicketResponse {

    private final UUID ticketId;
    private final String ticketNo;
    private final TicketType ticketType;
    private final AssetType assetType;
    private final UUID assetItemId;
    private final UUID requesterId;
    private final String requesterName;
    private final UUID departmentId;
    private final String departmentName;
    private final String itemName;
    private final String categoryName;
    private final Boolean isStandard;
    private final int quantity;
    private final BigDecimal estimatedUnitPrice;
    @JsonIgnore
    private final LocalDateTime requestedAt;

    public static PurchasePlanCandidateTicketResponse from(
            AssetRequestTicket assetRequestTicket,
            BigDecimal estimatedUnitPrice,
            int quantity
    ) {
        Ticket ticket = assetRequestTicket.getTicket();
        TangibleAssetItem tangibleItem = assetRequestTicket.getTangibleAssetItem();
        IntangibleAssetItem intangibleItem = assetRequestTicket.getIntangibleAssetItem();

        return PurchasePlanCandidateTicketResponse.builder()
                .ticketId(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .ticketType(ticket.getTicketType())
                .assetType(tangibleItem != null ? AssetType.TANGIBLE : AssetType.INTANGIBLE)
                .assetItemId(resolveAssetItemId(tangibleItem, intangibleItem))
                .requesterId(ticket.getRequester().getId())
                .requesterName(ticket.getRequester().getName())
                .departmentId(ticket.getDepartment().getId())
                .departmentName(ticket.getDepartment().getName())
                .itemName(tangibleItem != null ? tangibleItem.getProductName() : intangibleItem.getProductName())
                .categoryName(resolveCategoryName(tangibleItem, intangibleItem))
                .isStandard(resolveIsStandard(tangibleItem, intangibleItem))
                .quantity(quantity)
                .estimatedUnitPrice(estimatedUnitPrice)
                .requestedAt(ticket.getCreatedAt())
                .build();
    }

    public static PurchasePlanCandidateTicketResponse from(PurchaseRequestTicket purchaseRequestTicket) {
        Ticket ticket = purchaseRequestTicket.getTicket();

        return PurchasePlanCandidateTicketResponse.builder()
                .ticketId(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .ticketType(ticket.getTicketType())
                .assetType(resolveAssetType(purchaseRequestTicket))
                .assetItemId(resolveAssetItemId(purchaseRequestTicket))
                .requesterId(resolveRequesterId(ticket.getRequester()))
                .requesterName(resolveRequesterName(ticket.getRequester()))
                .departmentId(ticket.getDepartment().getId())
                .departmentName(ticket.getDepartment().getName())
                .itemName(resolveItemName(purchaseRequestTicket))
                .categoryName(resolveCategoryName(purchaseRequestTicket))
                .isStandard(purchaseRequestTicket.getIsStandard())
                .quantity(purchaseRequestTicket.getQuantity())
                .estimatedUnitPrice(purchaseRequestTicket.getExpectedPrice())
                .requestedAt(ticket.getCreatedAt())
                .build();
    }

    private static UUID resolveAssetItemId(TangibleAssetItem tangibleItem, IntangibleAssetItem intangibleItem) {
        if (tangibleItem != null) {
            return tangibleItem.getId();
        }
        return intangibleItem.getId();
    }

    private static Boolean resolveIsStandard(TangibleAssetItem tangibleItem, IntangibleAssetItem intangibleItem) {
        if (tangibleItem != null) {
            return tangibleItem.getIsStandard();
        }
        return intangibleItem.getIsStandard();
    }

    private static UUID resolveAssetItemId(PurchaseRequestTicket ticket) {
        if (ticket.getTangibleAssetItem() != null) {
            return ticket.getTangibleAssetItem().getId();
        }
        if (ticket.getIntangibleAssetItem() != null) {
            return ticket.getIntangibleAssetItem().getId();
        }
        return null;
    }

    private static String resolveCategoryName(TangibleAssetItem tangibleItem, IntangibleAssetItem intangibleItem) {
        if (tangibleItem != null) {
            return tangibleItem.getTangibleAssetCategory().getName();
        }
        return intangibleItem.getIntangibleAssetCategory().getName();
    }

    private static AssetType resolveAssetType(PurchaseRequestTicket ticket) {
        if (ticket.getTangibleAssetItem() != null || ticket.getTangibleAssetCategory() != null) {
            return AssetType.TANGIBLE;
        }
        return AssetType.INTANGIBLE;
    }

    private static String resolveItemName(PurchaseRequestTicket ticket) {
        if (ticket.getTangibleAssetItem() != null) {
            return ticket.getTangibleAssetItem().getProductName();
        }
        if (ticket.getIntangibleAssetItem() != null) {
            return ticket.getIntangibleAssetItem().getProductName();
        }
        return ticket.getRequestedItemDetail();
    }

    private static String resolveCategoryName(PurchaseRequestTicket ticket) {
        TangibleAssetCategory tangibleCategory = ticket.getTangibleAssetCategory();
        if (tangibleCategory != null) {
            return tangibleCategory.getName();
        }
        if (ticket.getTangibleAssetItem() != null) {
            return ticket.getTangibleAssetItem().getTangibleAssetCategory().getName();
        }

        IntangibleAssetCategory intangibleCategory = ticket.getIntangibleAssetCategory();
        if (intangibleCategory != null) {
            return intangibleCategory.getName();
        }
        return ticket.getIntangibleAssetItem().getIntangibleAssetCategory().getName();
    }

    private static UUID resolveRequesterId(Member requester) {
        return requester == null ? null : requester.getId();
    }

    private static String resolveRequesterName(Member requester) {
        return requester == null ? null : requester.getName();
    }
}
