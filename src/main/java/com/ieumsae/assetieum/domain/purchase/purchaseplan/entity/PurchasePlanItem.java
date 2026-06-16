package com.ieumsae.assetieum.domain.purchase.purchaseplan.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_plan_items")
public class PurchasePlanItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PurchasePlan purchasePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private PurchaseRequestTicket purchaseRequestTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intangible_asset_item_id")
    private IntangibleAssetItem intangibleAssetItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tangible_asset_item_id")
    private TangibleAssetItem tangibleAssetItem;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_standard", nullable = false)
    private Boolean isStandard;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "estimated_unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "actual_unit_price", precision = 15, scale = 2)
    private BigDecimal actualUnitPrice;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}
