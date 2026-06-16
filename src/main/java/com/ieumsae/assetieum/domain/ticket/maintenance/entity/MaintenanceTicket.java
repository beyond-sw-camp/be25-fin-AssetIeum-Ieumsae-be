package com.ieumsae.assetieum.domain.ticket.maintenance.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
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
@Table(name = "maintenance_tickets")
public class MaintenanceTicket {

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
	@Column(name = "maintenance_ticket_status", nullable = false, length = 50)
	private MaintenanceTicketStatus status;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tangible_asset_id", nullable = false)
	private TangibleAsset tangibleAsset;

	@Column(name = "collected_at")
	private LocalDateTime collectedAt;

	@Column(name = "maintenance_result", length = 255)
	private String maintenanceResult;

	@Column(name = "maintenance_completed_at")
	private LocalDateTime maintenanceCompletedAt;

	@Column(name = "maintenance_cost", precision = 15, scale = 2)
	private BigDecimal maintenanceCost;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static MaintenanceTicket createRequest(
		Ticket ticket,
		Company company,
		TangibleAsset tangibleAsset
	) {
		return MaintenanceTicket.builder()
			.ticket(ticket)
			.company(company)
			.status(MaintenanceTicketStatus.REQUESTED)
			.tangibleAsset(tangibleAsset)
			.build();
	}
}
