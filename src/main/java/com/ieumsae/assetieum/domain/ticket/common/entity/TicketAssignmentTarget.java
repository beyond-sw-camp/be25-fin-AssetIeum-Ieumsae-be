package com.ieumsae.assetieum.domain.ticket.common.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketAssignmentTargetStatus;
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
import java.time.LocalDateTime;
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
@Table(name = "ticket_assignment_targets")
public class TicketAssignmentTarget extends BaseEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_status", nullable = false, length = 30)
	private TicketAssignmentTargetStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "assigned_asset_type", length = 30)
	private AssetType assignedAssetType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "assigned_asset_id", columnDefinition = "CHAR(36)")
	private UUID assignedAssetId;

	@Column(name = "assigned_at")
	private LocalDateTime assignedAt;

	public static TicketAssignmentTarget create(Ticket ticket, Member member) {
		return TicketAssignmentTarget.builder()
			.ticket(ticket)
			.company(ticket.getCompany())
			.member(member)
			.status(TicketAssignmentTargetStatus.PENDING)
			.build();
	}

	public void markAssigned(AssetType assetType, UUID assetId, LocalDateTime assignedAt) {
		this.status = TicketAssignmentTargetStatus.ASSIGNED;
		this.assignedAssetType = assetType;
		this.assignedAssetId = assetId;
		this.assignedAt = assignedAt;
	}
}
