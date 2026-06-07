package com.ieumsae.assetieum.domain.department.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "departments")
public class Department extends BaseEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "department_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_department_id")
	private Department parentDepartment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_manager_id")
	private Member departmentManager;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected Department() {
	}

	public Department(Company company, Department parentDepartment, Member departmentManager, String name) {
		this.company = company;
		this.parentDepartment = parentDepartment;
		this.departmentManager = departmentManager;
		this.name = name;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void update(String name, Member departmentManager) {
		this.name = name;
		this.departmentManager = departmentManager;
	}

	public LocalDateTime delete() {
		this.deletedAt = LocalDateTime.now();
		return this.deletedAt;
	}
}
