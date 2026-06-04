package com.ieumsae.assetieum.domain.company;

import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "company_id", columnDefinition = "CHAR(36)")
	private UUID id;

	@Column(name = "company_code", nullable = false, length = 100)
	private String companyCode;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected Company() {
	}

	public Company(String companyCode) {
		this.companyCode = companyCode;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}
