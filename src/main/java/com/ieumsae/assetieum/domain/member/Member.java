package com.ieumsae.assetieum.domain.member;

import com.ieumsae.assetieum.domain.company.Company;
import com.ieumsae.assetieum.domain.department.Department;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "members")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "member_no", nullable = false, length = 100)
	private String employeeNumber;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private MemberRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_status", nullable = false, length = 50)
	private MemberStatus status;

	@Column(length = 255)
	private String email;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected Member() {
	}

	public Member(
		Company company,
		Department department,
		String employeeNumber,
		String password,
		String name,
		MemberRole role,
		String email
	) {
		this.company = company;
		this.department = department;
		this.employeeNumber = employeeNumber;
		this.password = password;
		this.name = name;
		this.role = role;
		this.status = MemberStatus.ACTIVE;
		this.email = email;
	}

	public boolean isActive() {
		return status == MemberStatus.ACTIVE && deletedAt == null;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}
}
