package com.ieumsae.assetieum.domain.tangibleasset.item.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tangible_asset_items")
public class TangibleAssetItem extends BaseEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tangible_asset_item_id", nullable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TangibleAssetCategory tangibleAssetCategory;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "is_standard", nullable = false)
    private Boolean isStandard;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(
            TangibleAssetItemUpdateRequest request,
            TangibleAssetCategory category
    ) {
        if(request.getProductName() != null) {
            this.productName = request.getProductName();
        }
        if(request.getModelName() != null) {
            this.modelName = request.getModelName();
        }
        if(category != null) {
            this.tangibleAssetCategory = category;
        }
        if(request.getManufacturer() != null) {
            this.manufacturer = request.getManufacturer();
        }
        if(request.getIsStandard() != null) {
            this.isStandard = request.getIsStandard();
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
