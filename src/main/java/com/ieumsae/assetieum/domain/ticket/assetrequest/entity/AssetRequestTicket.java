package com.ieumsae.assetieum.domain.ticket.assetrequest.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
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
@Table(name = "asset_request_tickets")
public class AssetRequestTicket {

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
	@Column(name = "asset_request_ticket_status", nullable = false, length = 50)
	private AssetRequestTicketStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_usage_type", nullable = false, length = 30)
	private RequestedUsageType requestedUsageType;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "expected_price", precision = 15, scale = 2)
	private BigDecimal estimatedUnitPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tangible_asset_item_id")
	private TangibleAssetItem tangibleAssetItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "intangible_asset_item_id")
	private IntangibleAssetItem intangibleAssetItem;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static AssetRequestTicket createRequest(
		Ticket ticket,
		Company company,
		RequestedUsageType requestedUsageType,
		TangibleAssetItem tangibleAssetItem,
		IntangibleAssetItem intangibleAssetItem,
		int quantity,
		BigDecimal estimatedUnitPrice
	) {
		return AssetRequestTicket.builder()
			.ticket(ticket)
			.company(company)
			.status(AssetRequestTicketStatus.REQUESTED)
			.requestedUsageType(requestedUsageType)
			.tangibleAssetItem(tangibleAssetItem)
			.intangibleAssetItem(intangibleAssetItem)
			.quantity(quantity)
			.estimatedUnitPrice(estimatedUnitPrice)
			.build();
	}

	public void markAssigned() {
		this.status = AssetRequestTicketStatus.ASSIGNED;
	}

	public void complete() {
		this.status = AssetRequestTicketStatus.COMPLETED;
	}

	public void cancel() {
		this.status = AssetRequestTicketStatus.CANCELLED;
	}
}
