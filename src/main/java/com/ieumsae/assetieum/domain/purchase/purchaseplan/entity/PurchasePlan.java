package com.ieumsae.assetieum.domain.purchase.purchaseplan.entity;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
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

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_plans")
public class PurchasePlan extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "plan_id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @Column(name = "plan_no", nullable = false, length = 100)
    private String planNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_request_status", nullable = false, length = 20)
    private PurchaseRequestStatus purchaseRequestStatus;

    @Column(name = "estimated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "item_count", nullable = false)
    @Builder.Default
    private Integer itemCount = 0;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public LocalDateTime delete() {
        this.purchaseRequestStatus = PurchaseRequestStatus.CANCELLED;

        this.deletedAt = KstDateTime.now();
        return this.deletedAt;
    }

    public void updateStatus(PurchaseRequestStatus status) {
        this.purchaseRequestStatus = status;

        if (status == PurchaseRequestStatus.APPROVED) {
            this.approvedAt = KstDateTime.now();
        }

        if (status == PurchaseRequestStatus.ORDERED) {
            this.orderedAt = KstDateTime.now();
        }

        if (status == PurchaseRequestStatus.DELIVERED) {
            this.deliveryDate = KstDateTime.now();
        }
    }

    public void updateActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
}
