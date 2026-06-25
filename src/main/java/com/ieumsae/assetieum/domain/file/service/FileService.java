package com.ieumsae.assetieum.domain.file.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.file.dto.FileUploadResponse;
import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import com.ieumsae.assetieum.domain.file.repository.UploadedFileRepository;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.s3.S3UploadResult;
import com.ieumsae.assetieum.global.s3.S3Uploader;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

	private final ObjectProvider<S3Uploader> s3UploaderProvider;
	private final UploadedFileRepository uploadedFileRepository;
	private final CompanyRepository companyRepository;
	private final MemberRepository memberRepository;
	private final TicketRepository ticketRepository;

	@Transactional
	public FileUploadResponse upload(
		MultipartFile file,
		FileTargetType targetType,
		UUID targetId,
		UUID companyId,
		UUID uploaderId
	) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
		Member uploader = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(uploaderId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		validateTarget(targetType, targetId, companyId);

		S3Uploader s3Uploader = s3UploaderProvider.getIfAvailable();
		if (s3Uploader == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "S3 파일 업로드 설정이 활성화되어 있지 않습니다.");
		}

		S3UploadResult uploadResult = s3Uploader.upload(file, targetType.name().toLowerCase());
		UploadedFile uploadedFile = uploadedFileRepository.save(UploadedFile.builder()
			.company(company)
			.targetType(targetType)
			.targetId(targetId)
			.name(resolveOriginalFilename(uploadResult.originalFilename()))
			.path(uploadResult.url())
			.fileSize(uploadResult.size())
			.extension(resolveExtension(uploadResult.originalFilename()))
			.uploader(uploader)
			.build());

		return FileUploadResponse.from(uploadedFile);
	}

	private void validateTarget(FileTargetType targetType, UUID targetId, UUID companyId) {
		if (targetType == null || targetId == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		if (targetType == FileTargetType.TICKET) {
			ticketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(targetId, companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		}
	}

	private String resolveOriginalFilename(String originalFilename) {
		if (!StringUtils.hasText(originalFilename)) {
			return "file";
		}
		String normalized = originalFilename.replace("\\", "/");
		return normalized.substring(normalized.lastIndexOf('/') + 1);
	}

	private String resolveExtension(String originalFilename) {
		if (!StringUtils.hasText(originalFilename)) {
			return "";
		}
		String filename = resolveOriginalFilename(originalFilename);
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			return "";
		}
		return filename.substring(dotIndex + 1).toLowerCase();
	}
}
