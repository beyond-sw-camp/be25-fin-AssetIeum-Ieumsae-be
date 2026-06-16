package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchasePlanItemCreateRequest {

    private UUID ticketId;

    @NotNull(message = "자산 유형은 필수입니다.")
    private AssetType assetType;

    @NotNull(message = "자산 품목 ID는 필수입니다.")
    private UUID assetItemId;

    @NotBlank(message = "품목명은 필수입니다.")
    @Size(max = 255, message = "품목명은 255자 이하여야 합니다.")
    private String itemName;

    private UUID departmentId;

    @NotNull(message = "표준 품목 여부는 필수입니다.")
    private Boolean isStandard;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotNull(message = "예정 단가는 필수입니다.")
    @DecimalMin(value = "0.00", message = "예정 단가는 0 이상이어야 합니다.")
    private BigDecimal estimatedUnitPrice;

    @URL(message = "외부 URL 형식이 올바르지 않습니다.")
    @Size(max = 500, message = "외부 URL은 500자 이하여야 합니다.")
    private String externalUrl;

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public void setAssetItemId(UUID assetItemId) {
        this.assetItemId = assetItemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public void setIsStandard(Object isStandard) {
        if (isStandard instanceof Boolean value) {
            this.isStandard = value;
            return;
        }

        if (isStandard instanceof Number value) {
            this.isStandard = value.intValue() == 1;
            return;
        }

        if (isStandard instanceof String value) {
            this.isStandard = value.equals("1") || Boolean.parseBoolean(value);
            return;
        }

        this.isStandard = null;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) {
        this.estimatedUnitPrice = estimatedUnitPrice;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }
}
