package com.ieumsae.assetieum.domain.ticket.common.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
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
@Table(name = "tickets")
public class Ticket extends BaseEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "ticket_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "ticket_no", nullable = false, length = 50)
	private String ticketNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "ticket_type", nullable = false, length = 50)
	private TicketType ticketType;

	@Enumerated(EnumType.STRING)
	@Column(name = "ticket_status", nullable = false, length = 50)
	private TicketStatus ticketStatus;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requester_id", nullable = false)
	private Member requester;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "approver_id", nullable = false)
	private Member approver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private Member assignee;

	@Column(name = "request_reason", length = 255)
	private String requestReason;

	@Column(name = "department_approved_at")
	private LocalDateTime departmentApprovedAt;

	@Column(name = "department_rejected_at")
	private LocalDateTime departmentRejectedAt;

	@Column(name = "department_rejection_reason", length = 255)
	private String departmentRejectionReason;

	@Column(name = "purchase_approved_at")
	private LocalDateTime purchaseApprovedAt;

	@Column(name = "purchase_rejected_at")
	private LocalDateTime purchaseRejectedAt;

	@Column(name = "purchase_rejection_reason", length = 255)
	private String purchaseRejectionReason;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static Ticket createAssetRequest(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.ASSET_REQUEST)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createPurchaseRequest(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.PURCHASE_REQUEST)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createRental(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.RENTAL)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createRentalExtension(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.RENTAL_EXTENSION)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createMaintenanceRequest(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.MAINTENANCE_REQUEST)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createAssetReturn(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.ASSET_RETURN)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public static Ticket createPurchaseReturn(
		Company company,
		String ticketNo,
		Member requester,
		Department department,
		Member approver,
		String requestReason
	) {
		return Ticket.builder()
			.company(company)
			.ticketNo(ticketNo)
			.ticketType(TicketType.PURCHASE_RETURN)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(department)
			.approver(approver)
			.requestReason(requestReason)
			.build();
	}

	public void approveDepartment(LocalDateTime approvedAt) {
		this.ticketStatus = TicketStatus.DEPARTMENT_APPROVED;
		this.departmentApprovedAt = approvedAt;
		this.departmentRejectedAt = null;
		this.departmentRejectionReason = null;
	}

	public void rejectDepartment(String rejectionReason, LocalDateTime rejectedAt) {
		this.ticketStatus = TicketStatus.DEPARTMENT_REJECTED;
		this.departmentRejectedAt = rejectedAt;
		this.departmentRejectionReason = rejectionReason;
	}

	public void assign(Member assignee) {
		this.assignee = assignee;
	}

	public void approveAsset(Member assignee, LocalDateTime approvedAt) {
		this.ticketStatus = TicketStatus.IN_PROGRESS;
		this.assignee = assignee;
		this.purchaseApprovedAt = approvedAt;
		this.purchaseRejectedAt = null;
		this.purchaseRejectionReason = null;
	}

	public void rejectAsset(Member assignee, String rejectionReason, LocalDateTime rejectedAt) {
		this.ticketStatus = TicketStatus.ASSET_REJECTED;
		this.assignee = assignee;
		this.purchaseRejectedAt = rejectedAt;
		this.purchaseRejectionReason = rejectionReason;
	}

	public void cancel(LocalDateTime cancelledAt) {
		this.ticketStatus = TicketStatus.CANCELLED;
		this.cancelledAt = cancelledAt;
	}

	public void changeProcessingStatus(TicketStatus status, LocalDateTime processedAt) {
		this.ticketStatus = status;
		if (status == TicketStatus.IN_PROGRESS) {
			this.completedAt = null;
			this.cancelledAt = null;
			return;
		}
		if (status == TicketStatus.COMPLETED) {
			this.completedAt = processedAt;
			this.cancelledAt = null;
			return;
		}
		if (status == TicketStatus.CANCELLED) {
			this.cancelledAt = processedAt;
			this.completedAt = null;
		}
	}
}
