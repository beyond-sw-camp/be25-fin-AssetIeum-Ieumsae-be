package com.ieumsae.assetieum.domain.ticket.purchaserequest.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.global.common.BaseEntity;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
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
@Table(name = "direct_purchase_results")
public class DirectPurchaseResult extends BaseEntity {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "ticket_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private PurchaseRequestTicket purchaseRequestTicket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submitter_id", nullable = false)
	private Member submitter;

	@Column(name = "actual_price", nullable = false, precision = 15, scale = 2)
	private BigDecimal actualPrice;

	@Column(name = "purchase_date", nullable = false)
	private LocalDateTime purchaseDate;

	@Column(name = "purchase_vendor", nullable = false, length = 150)
	private String purchaseVendor;

	@Column(name = "serial_number", columnDefinition = "TEXT")
	private String serialNumber;

	@Column(name = "location", length = 150)
	private String location;

	@Column(name = "warranty_expired_at")
	private LocalDateTime warrantyExpiredAt;

	@Column(name = "license_code", columnDefinition = "TEXT")
	private String licenseCode;

	@Column(name = "seat_count")
	private Integer seatCount;

	@Column(name = "is_auto_renewal")
	private Boolean isAutoRenewal;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "expired_at")
	private LocalDateTime expiredAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", length = 30)
	private BillingCycle billingCycle;

	@Column(name = "proof_file_url", length = 500)
	private String proofFileUrl;

	@Column(name = "proof_file_uploaded_at")
	private LocalDateTime proofFileUploadedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "confirmation_status", nullable = false, length = 30)
	private ConfirmationStatus confirmationStatus;

	public static DirectPurchaseResult create(
		PurchaseRequestTicket purchaseRequestTicket,
		Member submitter,
		BigDecimal actualPrice,
		LocalDateTime purchaseDate,
		String purchaseVendor,
		String serialNumber,
		String location,
		LocalDateTime warrantyExpiredAt,
		String licenseCode,
		Integer seatCount,
		Boolean isAutoRenewal,
		LocalDateTime startedAt,
		LocalDateTime expiredAt,
		BillingCycle billingCycle
	) {
		return DirectPurchaseResult.builder()
			.purchaseRequestTicket(purchaseRequestTicket)
			.company(submitter.getCompany())
			.submitter(submitter)
			.actualPrice(actualPrice)
			.purchaseDate(purchaseDate)
			.purchaseVendor(purchaseVendor)
			.serialNumber(serialNumber)
			.location(location)
			.warrantyExpiredAt(warrantyExpiredAt)
			.licenseCode(licenseCode)
			.seatCount(seatCount)
			.isAutoRenewal(isAutoRenewal)
			.startedAt(startedAt)
			.expiredAt(expiredAt)
			.billingCycle(billingCycle)
			.confirmationStatus(ConfirmationStatus.PENDING)
			.build();
	}

	public void confirm() {
		this.confirmationStatus = ConfirmationStatus.CONFIRMED;
	}

	public void updateProofFile(String proofFileUrl, LocalDateTime uploadedAt) {
		this.proofFileUrl = proofFileUrl;
		this.proofFileUploadedAt = uploadedAt;
	}

	public void update(
		BigDecimal actualPrice,
		LocalDateTime purchaseDate,
		String purchaseVendor,
		String serialNumber,
		String location,
		LocalDateTime warrantyExpiredAt,
		String licenseCode,
		Integer seatCount,
		Boolean isAutoRenewal,
		LocalDateTime startedAt,
		LocalDateTime expiredAt,
		BillingCycle billingCycle
	) {
		this.actualPrice = actualPrice;
		this.purchaseDate = purchaseDate;
		this.purchaseVendor = purchaseVendor;
		this.serialNumber = serialNumber;
		this.location = location;
		this.warrantyExpiredAt = warrantyExpiredAt;
		this.licenseCode = licenseCode;
		this.seatCount = seatCount;
		this.isAutoRenewal = isAutoRenewal;
		this.startedAt = startedAt;
		this.expiredAt = expiredAt;
		this.billingCycle = billingCycle;
	}
}
