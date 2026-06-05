package com.ieumsae.assetieum.domain.member.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
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
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "members")
public class Member extends BaseEntity {

	@Id
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
	private String memberNo;

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
		String memberNo,
		String password,
		String name,
		MemberRole role,
		String email
	) {
		this.company = company;
		this.department = department;
		this.memberNo = memberNo;
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
