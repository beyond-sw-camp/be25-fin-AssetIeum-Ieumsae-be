package com.ieumsae.assetieum.domain.intangibleasset.item.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
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

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "intangible_asset_items")
public class IntangibleAssetItem extends BaseEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "intangible_asset_item_id", nullable = false, length = 36)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private IntangibleAssetCategory intangibleAssetCategory;

	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;

	@Column(length = 100)
	private String provider;

	@Column(name = "license_type", nullable = false, length = 30)
	private String licenseType;

	@Column(name = "is_standard", nullable = false)
	private Boolean isStandard;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}
