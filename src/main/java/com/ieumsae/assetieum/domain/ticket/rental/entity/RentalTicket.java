package com.ieumsae.assetieum.domain.ticket.rental.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.rental.type.RentalTicketStatus;
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
@Table(name = "rental_tickets")
public class RentalTicket {

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
	@Column(name = "rental_ticket_status", nullable = false, length = 50)
	private RentalTicketStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tangible_asset_id")
	private TangibleAsset tangibleAsset;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tangible_asset_item_id", nullable = false)
	private TangibleAssetItem tangibleAssetItem;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_usage_type", nullable = false, length = 30)
	private RequestedUsageType requestedUsageType;

	@Column(name = "rental_start_date", nullable = false)
	private LocalDateTime rentalStartDate;

	@Column(name = "rental_due_date")
	private LocalDateTime rentalDueDate;

	@Column(name = "requested_due_date")
	private LocalDateTime requestedDueDate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static RentalTicket createRequest(
		Ticket ticket,
		Company company,
		RequestedUsageType requestedUsageType,
		TangibleAssetItem tangibleAssetItem,
		LocalDateTime rentalStartDate,
		LocalDateTime requestedDueDate
	) {
		return RentalTicket.builder()
			.ticket(ticket)
			.company(company)
			.status(RentalTicketStatus.REQUESTED)
			.requestedUsageType(requestedUsageType)
			.tangibleAssetItem(tangibleAssetItem)
			.rentalStartDate(rentalStartDate)
			.requestedDueDate(requestedDueDate)
			.build();
	}

	public static RentalTicket createExtensionRequest(
		Ticket ticket,
		Company company,
		RequestedUsageType requestedUsageType,
		TangibleAsset tangibleAsset,
		TangibleAssetItem tangibleAssetItem,
		LocalDateTime rentalStartDate,
		LocalDateTime currentDueDate,
		LocalDateTime requestedDueDate
	) {
		return RentalTicket.builder()
			.ticket(ticket)
			.company(company)
			.status(RentalTicketStatus.EXTENSION_REQUESTED)
			.requestedUsageType(requestedUsageType)
			.tangibleAsset(tangibleAsset)
			.tangibleAssetItem(tangibleAssetItem)
			.rentalStartDate(rentalStartDate)
			.rentalDueDate(currentDueDate)
			.requestedDueDate(requestedDueDate)
			.build();
	}

	public void reserveAsset(TangibleAsset tangibleAsset) {
		this.tangibleAsset = tangibleAsset;
		this.status = RentalTicketStatus.RESERVED;
	}

	public void markAssigned() {
		this.status = RentalTicketStatus.ASSIGNED;
	}

	public void complete() {
		this.status = RentalTicketStatus.COMPLETED;
	}

	public void cancelReservation() {
		this.status = RentalTicketStatus.CANCELLED;
	}
}
