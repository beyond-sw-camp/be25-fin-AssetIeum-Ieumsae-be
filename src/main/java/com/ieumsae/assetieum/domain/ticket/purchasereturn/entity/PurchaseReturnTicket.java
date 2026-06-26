package com.ieumsae.assetieum.domain.ticket.purchasereturn.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnType;
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
@Table(name = "purchase_return_tickets")
public class PurchaseReturnTicket {

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
	@Column(name = "purchase_return_type", nullable = false, length = 50)
	private PurchaseReturnType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "purchase_return_ticket_status", nullable = false, length = 50)
	private PurchaseReturnTicketStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "intangible_asset_id")
	private IntangibleAsset intangibleAsset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tangible_asset_id")
	private TangibleAsset tangibleAsset;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "collected_at")
	private LocalDateTime collectedAt;

	@Column(name = "shipped_at")
	private LocalDateTime shippedAt;

	public static PurchaseReturnTicket createTangibleReturn(Ticket ticket, Company company, TangibleAsset tangibleAsset) {
		return PurchaseReturnTicket.builder()
			.ticket(ticket)
			.company(company)
			.type(PurchaseReturnType.DIRECT_RETURN)
			.status(PurchaseReturnTicketStatus.REQUESTED)
			.tangibleAsset(tangibleAsset)
			.build();
	}

	public static PurchaseReturnTicket createIntangibleReturn(Ticket ticket, Company company, IntangibleAsset intangibleAsset) {
		return PurchaseReturnTicket.builder()
			.ticket(ticket)
			.company(company)
			.type(PurchaseReturnType.DIRECT_RETURN)
			.status(PurchaseReturnTicketStatus.REQUESTED)
			.intangibleAsset(intangibleAsset)
			.build();
	}

	public void collect(LocalDateTime collectedAt) {
		this.status = PurchaseReturnTicketStatus.COLLECTED;
		this.collectedAt = collectedAt;
	}

	public void complete(LocalDateTime processedAt) {
		this.status = PurchaseReturnTicketStatus.COMPLETED;
		this.shippedAt = processedAt;
	}

	public void cancel() {
		this.status = PurchaseReturnTicketStatus.CANCELLED;
	}
}
