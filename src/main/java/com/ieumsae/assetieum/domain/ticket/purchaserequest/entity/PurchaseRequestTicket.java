package com.ieumsae.assetieum.domain.ticket.purchaserequest.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_request_tickets")
public class PurchaseRequestTicket {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "ticket_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "purchase_request_ticket_status", nullable = false, length = 50)
	private PurchaseRequestTicketStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_method", nullable = false, length = 50)
	private RequestMethod requestMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_usage_type", nullable = false, length = 30)
	private RequestedUsageType requestedUsageType;

	@Column(name = "is_standard", nullable = false)
	private Boolean isStandard;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tangible_asset_item_id")
	private TangibleAssetItem tangibleAssetItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "intangible_asset_item_id")
	private IntangibleAssetItem intangibleAssetItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tangible_asset_category_id")
	private TangibleAssetCategory tangibleAssetCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "intangible_asset_category_id")
	private IntangibleAssetCategory intangibleAssetCategory;

	@Column(name = "requested_item_detail", nullable = false, length = 500)
	private String requestedItemDetail;

	@Column(length = 100)
	private String manufacturer;

	@Enumerated(EnumType.STRING)
	@Column(name = "license_type", length = 50)
	private LicenseType licenseType;

	@Column(name = "purchase_url", length = 500)
	private String purchaseUrl;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "expected_price", precision = 15, scale = 2)
	private BigDecimal expectedPrice;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static PurchaseRequestTicket create(
		Ticket ticket,
		Company company,
		RequestMethod requestMethod,
		RequestedUsageType requestedUsageType,
		Boolean isStandard,
		TangibleAssetItem tangibleAssetItem,
		IntangibleAssetItem intangibleAssetItem,
		TangibleAssetCategory tangibleAssetCategory,
		IntangibleAssetCategory intangibleAssetCategory,
		String requestedItemDetail,
		String manufacturer,
		LicenseType licenseType,
		String purchaseUrl,
		int quantity,
		BigDecimal expectedPrice
	) {
		return PurchaseRequestTicket.builder()
			.ticket(ticket)
			.company(company)
			.status(PurchaseRequestTicketStatus.REQUESTED)
			.requestMethod(requestMethod)
			.requestedUsageType(requestedUsageType)
			.isStandard(isStandard)
			.tangibleAssetItem(tangibleAssetItem)
			.intangibleAssetItem(intangibleAssetItem)
			.tangibleAssetCategory(tangibleAssetCategory)
			.intangibleAssetCategory(intangibleAssetCategory)
			.requestedItemDetail(requestedItemDetail)
			.manufacturer(manufacturer)
			.licenseType(licenseType)
			.purchaseUrl(purchaseUrl)
			.quantity(quantity)
			.expectedPrice(expectedPrice)
			.build();
	}

	public void markOrdered() {
		this.status = PurchaseRequestTicketStatus.ORDERED;
	}

	public void markRequested() {
		this.status = PurchaseRequestTicketStatus.REQUESTED;
	}

	public void markReceived() {
		this.status = PurchaseRequestTicketStatus.RECEIVED;
	}

	public void complete() {
		this.status = PurchaseRequestTicketStatus.COMPLETED;
	}

	public void cancel() {
		this.status = PurchaseRequestTicketStatus.CANCELLED;
	}
}
