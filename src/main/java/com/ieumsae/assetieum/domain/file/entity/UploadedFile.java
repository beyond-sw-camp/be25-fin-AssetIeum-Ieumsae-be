package com.ieumsae.assetieum.domain.file.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class UploadedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 50)
	private FileTargetType targetType;

	@Column(name = "target_id", nullable = false, length = 36)
	private String targetId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "path", nullable = false, length = 500)
	private String path;

	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	@Column(name = "extension", nullable = false, length = 20)
	private String extension;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "uploader_id", nullable = false)
	private Member uploader;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
