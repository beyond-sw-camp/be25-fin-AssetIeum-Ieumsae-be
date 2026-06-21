package com.ieumsae.assetieum.domain.intangibleasset.asset.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "intangible_assets")
public class IntangibleAsset extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "intangible_asset_id", nullable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intangible_item_id", nullable = false)
    private IntangibleAssetItem intangibleAssetItem;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(name = "license_code", length = 50)
    private String licenseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "intangible_asset_status", nullable = false, length = 30)
    @Builder.Default
    private IntangibleAssetStatus intangibleAssetStatus = IntangibleAssetStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "is_auto_renewal")
    private Boolean isAutoRenewal;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycle billingCycle;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_vendor", length = 150)
    private String purchaseVendor;

    public void update(
            IntangibleAssetUpdateRequest request,
            Department department,
            Member member
    ) {
        if (request.getIntangibleAssetStatus() != null) {
            this.intangibleAssetStatus = request.getIntangibleAssetStatus();
        }
        if (request.getSeatCount() != null) {
            this.seatCount = request.getSeatCount();
        }
        if (request.getIsAutoRenewal() != null) {
            this.isAutoRenewal = request.getIsAutoRenewal();
        }
        if (request.getStartedAt() != null) {
            this.startedAt = request.getStartedAt();
        }
        if (request.getExpiredAt() != null) {
            this.expiredAt = request.getExpiredAt();
        }
        if (department != null) {
            this.department = department;
        }
        if (member != null) {
            this.member = member;
        }
    }

    public void assignTo(Member member, Department department) {
        this.member = member;
        this.department = department;
        this.intangibleAssetStatus = IntangibleAssetStatus.IN_USE;
    }

    public void markInUse() {
        this.intangibleAssetStatus = IntangibleAssetStatus.IN_USE;
    }

    public void clearAssignee() {
        this.member = null;
        this.department = null;
    }

    public void cancel() {
        clearAssignee();
        this.intangibleAssetStatus = IntangibleAssetStatus.CANCELLED;
    }

    public void restoreInUseAfterTicketCancel() {
        this.intangibleAssetStatus = IntangibleAssetStatus.IN_USE;
    }
}
