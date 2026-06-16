package com.ieumsae.assetieum.domain.purchase.purchasepolicy.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.type.PurchaseMethod;
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
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_policies")
public class PurchasePolicy extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "policy_id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method", nullable = false, length = 30)
    @Builder.Default
    private PurchaseMethod purchaseMethod = PurchaseMethod.PARALLEL;

    @Column(name = "over_percentage_limit", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overPercentageLimit = BigDecimal.ZERO;

    public void update(PurchaseMethod purchaseMethod, BigDecimal overPercentageLimit) {
        if (purchaseMethod != null) {
            this.purchaseMethod = purchaseMethod;
        }
        if (overPercentageLimit != null) {
            this.overPercentageLimit = overPercentageLimit;
        }
    }
}
