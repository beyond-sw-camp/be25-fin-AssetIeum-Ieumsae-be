package com.ieumsae.assetieum.domain.file.repository;

import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

	List<UploadedFile> findAllByCompany_IdAndTargetTypeAndTargetIdOrderByCreatedAtAsc(
		UUID companyId,
		FileTargetType targetType,
		String targetId
	);

	Optional<UploadedFile> findByIdAndCompany_Id(Long fileId, UUID companyId);
}
