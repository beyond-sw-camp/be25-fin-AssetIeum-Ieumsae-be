package com.ieumsae.assetieum.domain.tangibleasset.asset.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
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
@Table(name = "tangible_assets")
public class TangibleAsset extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tangible_asset_id", nullable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tangible_item_id", nullable = false)
    private TangibleAssetItem tangibleAssetItem;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", length = 30)
    private UsageType usageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_usage_type", length = 30)
    private AssetUsageType assetUsageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tangible_asset_status", nullable = false, length = 30)
    @Builder.Default
    private TangibleAssetStatus tangibleAssetStatus = TangibleAssetStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "used_started_at")
    private LocalDateTime usedStartedAt;

    @Column(name = "return_due_date")
    private LocalDateTime returnDueDate;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_vendor", length = 150)
    private String purchaseVendor;

    @Column(name = "warranty_expired_at")
    private LocalDateTime warrantyExpiredAt;

    public void update(
            TangibleAssetUpdateRequest request,
            Department department,
            Member member
    ) {
        if (request.getTangibleAssetStatus() != null) {
            this.tangibleAssetStatus = request.getTangibleAssetStatus();
        }
        if (request.getLocation() != null) {
            this.location = request.getLocation();
        }
        if (request.getUsedStartedAt() != null) {
            this.usedStartedAt = request.getUsedStartedAt();
        }
        if (request.getReturnDueDate() != null) {
            this.returnDueDate = request.getReturnDueDate();
        }
        if (request.getUsageType() != null) {
            this.usageType = request.getUsageType();
            if(request.getUsageType() == UsageType.PERMANENT){
                this.returnDueDate = null;
            }
        }
        if (department != null) {
            this.department = department;
        }
        if (member != null) {
            this.member = member;
        }
    }

    public void markInUse(
            Member member,
            Department department,
            UsageType usageType,
            AssetUsageType assetUsageType,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        this.member = member;
        this.department = department;
        this.usageType = usageType;
        this.assetUsageType = assetUsageType;
        this.tangibleAssetStatus = TangibleAssetStatus.IN_USE;
        if (this.usedStartedAt == null) {
            this.usedStartedAt = startedAt;
        }
        if (usageType == UsageType.PERMANENT) {
            this.returnDueDate = null;
        } else if(endedAt != null) {
            this.returnDueDate = endedAt;
        }
    }

    public void reserveForRental() {
        this.tangibleAssetStatus = TangibleAssetStatus.RESERVED;
    }

    public void releaseReservation() {
        this.member = null;
        this.department = null;
        this.usageType = null;
        this.assetUsageType = null;
        this.usedStartedAt = null;
        this.returnDueDate = null;
        this.tangibleAssetStatus = TangibleAssetStatus.AVAILABLE;
    }

    public void returnRequest() {
        this.member = null;
        this.department = null;
        this.usedStartedAt = null;
        this.returnDueDate = null;
        this.tangibleAssetStatus = TangibleAssetStatus.RETURN_REQUESTED;
    }

    public void requestReturn() {
        this.tangibleAssetStatus = TangibleAssetStatus.RETURN_REQUESTED;
    }

    public void collectReturn() {
        this.member = null;
        this.department = null;
        this.usageType = null;
        this.assetUsageType = null;
        this.usedStartedAt = null;
        this.returnDueDate = null;
        this.tangibleAssetStatus = TangibleAssetStatus.AVAILABLE;
    }

    public void completeReturn() {
        this.member = null;
        this.department = null;
        this.usageType = null;
        this.assetUsageType = null;
        this.usedStartedAt = null;
        this.returnDueDate = null;
        this.tangibleAssetStatus = TangibleAssetStatus.AVAILABLE;
    }

    public void restoreInUseAfterTicketCancel() {
        this.tangibleAssetStatus = TangibleAssetStatus.IN_USE;
    }

    public void requestRepair() {
        this.tangibleAssetStatus = TangibleAssetStatus.REPAIR_REQUESTED;
    }

    public void startRepair() {
        this.tangibleAssetStatus = TangibleAssetStatus.REPAIRING;
    }

    public void completeRepair() {
        this.tangibleAssetStatus = TangibleAssetStatus.IN_USE;
    }

    public void reassign(Member newMember, LocalDateTime reassignedAt) {
        this.member = newMember;
        this.usedStartedAt = reassignedAt;
    }

    public void updateReturnDueDate(LocalDateTime returnDueDate) {
        this.returnDueDate = returnDueDate;
    }
}
