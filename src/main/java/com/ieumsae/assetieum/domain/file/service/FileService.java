package com.ieumsae.assetieum.domain.file.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.file.dto.FileDownloadUrlResponse;
import com.ieumsae.assetieum.domain.file.dto.FileResponse;
import com.ieumsae.assetieum.domain.file.dto.FileUploadResponse;
import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import com.ieumsae.assetieum.domain.file.repository.UploadedFileRepository;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.DirectPurchaseResultRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.s3.S3UploadResult;
import com.ieumsae.assetieum.global.s3.S3Uploader;
import java.time.Duration;
import java.util.List;
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

	private static final long DOWNLOAD_URL_EXPIRES_IN_SECONDS = 300;

	private final ObjectProvider<S3Uploader> s3UploaderProvider;
	private final UploadedFileRepository uploadedFileRepository;
	private final CompanyRepository companyRepository;
	private final MemberRepository memberRepository;
	private final TicketRepository ticketRepository;
	private final DirectPurchaseResultRepository directPurchaseResultRepository;
	private final PurchasePlanItemRepository purchasePlanItemRepository;

	@Transactional
	public FileUploadResponse upload(
		MultipartFile file,
		FileTargetType targetType,
		String targetId,
		UUID companyId,
		UUID uploaderId
	) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
		Member uploader = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(uploaderId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		validateTarget(targetType, targetId, companyId);

		S3Uploader s3Uploader = resolveS3Uploader();
		if (s3Uploader == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "S3 파일 업로드 설정이 활성화되어 있지 않습니다.");
		}

		S3UploadResult uploadResult;
		try {
			uploadResult = s3Uploader.upload(file, targetType.name().toLowerCase());
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
		}
		UploadedFile uploadedFile;
		try {
			uploadedFile = uploadedFileRepository.save(UploadedFile.builder()
				.company(company)
				.targetType(targetType)
				.targetId(normalizeTargetId(targetId))
				.name(resolveOriginalFilename(uploadResult.originalFilename()))
				.path(uploadResult.url())
				.fileSize(uploadResult.size())
				.extension(resolveExtension(uploadResult.originalFilename()))
				.uploader(uploader)
				.build());
			linkEvidenceIfNeeded(targetType, targetId, companyId, uploadedFile);
		} catch (RuntimeException e) {
			s3Uploader.delete(uploadResult.key());
			throw e;
		}

		return FileUploadResponse.from(uploadedFile);
	}

	@Transactional(readOnly = true)
	public List<FileResponse> getFiles(FileTargetType targetType, String targetId, UUID companyId) {
		validateTarget(targetType, targetId, companyId);
		return uploadedFileRepository
			.findAllByCompany_IdAndTargetTypeAndTargetIdOrderByCreatedAtAsc(
				companyId,
				targetType,
				normalizeTargetId(targetId)
			)
			.stream()
			.map(FileResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public FileDownloadUrlResponse createDownloadUrl(Long fileId, UUID companyId, UUID memberId) {
		UploadedFile file = uploadedFileRepository.findByIdAndCompany_Id(fileId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
		Member viewer = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		validateFileReadable(file, viewer, companyId);

		String downloadUrl = resolveS3Uploader().createPresignedGetUrl(
			file.getPath(),
			Duration.ofSeconds(DOWNLOAD_URL_EXPIRES_IN_SECONDS)
		);
		return FileDownloadUrlResponse.of(downloadUrl, DOWNLOAD_URL_EXPIRES_IN_SECONDS);
	}

	@Transactional
	public void deleteFile(Long fileId, UUID companyId) {
		UploadedFile file = uploadedFileRepository.findByIdAndCompany_Id(fileId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일을 찾을 수 없습니다."));
		S3Uploader s3Uploader = resolveS3Uploader();
		validateEvidenceDeletable(file, companyId);
		s3Uploader.deleteByUrl(file.getPath());
		unlinkEvidenceIfNeeded(file, companyId);
		uploadedFileRepository.delete(file);
	}

	private void validateTarget(FileTargetType targetType, String targetId, UUID companyId) {
		if (targetType == null || !StringUtils.hasText(targetId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		switch (targetType) {
			case TICKET -> ticketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(parseUuid(targetId), companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
			case DIRECT_PURCHASE_RESULT -> directPurchaseResultRepository.findByIdAndCompany_Id(parseUuid(targetId), companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
			case PURCHASE_PLAN_ITEM -> purchasePlanItemRepository.findByIdAndCompany_Id(parseLong(targetId), companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));
		}
	}

	private void validateFileReadable(UploadedFile file, Member viewer, UUID companyId) {
		switch (file.getTargetType()) {
			case DIRECT_PURCHASE_RESULT -> validateDirectPurchaseFileReadable(file, viewer, companyId);
			case PURCHASE_PLAN_ITEM -> validatePurchasePlanFileReadable(file, viewer, companyId);
			case TICKET -> validateTicketFileReadable(file, viewer, companyId);
		}
	}

	private void validateDirectPurchaseFileReadable(UploadedFile file, Member viewer, UUID companyId) {
		DirectPurchaseResult result = directPurchaseResultRepository
			.findByIdAndCompany_Id(parseUuid(file.getTargetId()), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		Ticket ticket = result.getPurchaseRequestTicket().getTicket();

		if (ticket.getRequester().getId().equals(viewer.getId()) || isAssetRole(viewer.getRole())) {
			return;
		}
		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validatePurchasePlanFileReadable(UploadedFile file, Member viewer, UUID companyId) {
		purchasePlanItemRepository.findByIdAndCompany_Id(parseLong(file.getTargetId()), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));
		if (isAssetRole(viewer.getRole())) {
			return;
		}
		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validateTicketFileReadable(UploadedFile file, Member viewer, UUID companyId) {
		Ticket ticket = ticketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(parseUuid(file.getTargetId()), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		if (ticket.getRequester().getId().equals(viewer.getId())
			|| ticket.getApprover().getId().equals(viewer.getId())
			|| isAssetRole(viewer.getRole())) {
			return;
		}
		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private boolean isAssetRole(MemberRole role) {
		return role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private S3Uploader resolveS3Uploader() {
		S3Uploader s3Uploader = s3UploaderProvider.getIfAvailable();
		if (s3Uploader == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "S3 파일 업로드 설정이 활성화되어 있지 않습니다.");
		}
		return s3Uploader;
	}

	private void linkEvidenceIfNeeded(
		FileTargetType targetType,
		String targetId,
		UUID companyId,
		UploadedFile uploadedFile
	) {
		if (targetType != FileTargetType.DIRECT_PURCHASE_RESULT) {
			return;
		}
		DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(parseUuid(targetId), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		validateDirectPurchaseProofEditable(result);
		result.updateProofFile(uploadedFile.getPath(), uploadedFile.getCreatedAt());
	}

	private void validateEvidenceDeletable(UploadedFile file, UUID companyId) {
		if (file.getTargetType() != FileTargetType.DIRECT_PURCHASE_RESULT) {
			return;
		}
		DirectPurchaseResult result = directPurchaseResultRepository
			.findByIdAndCompany_Id(parseUuid(file.getTargetId()), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		validateDirectPurchaseProofEditable(result);
	}

	private void validateDirectPurchaseProofEditable(DirectPurchaseResult result) {
		if (result.getConfirmationStatus() == ConfirmationStatus.CONFIRMED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 확인 완료 후에는 증빙 파일을 수정할 수 없습니다.");
		}
	}

	private void unlinkEvidenceIfNeeded(UploadedFile file, UUID companyId) {
		if (file.getTargetType() != FileTargetType.DIRECT_PURCHASE_RESULT) {
			return;
		}
		DirectPurchaseResult result = directPurchaseResultRepository
			.findByIdAndCompany_Id(parseUuid(file.getTargetId()), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		if (file.getPath().equals(result.getProofFileUrl())) {
			result.updateProofFile(null, null);
		}
	}

	private String normalizeTargetId(String targetId) {
		return targetId.trim();
	}

	private UUID parseUuid(String value) {
		try {
			return UUID.fromString(value.trim());
		} catch (RuntimeException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "targetId 형식이 올바르지 않습니다.");
		}
	}

	private Long parseLong(String value) {
		try {
			return Long.valueOf(value.trim());
		} catch (RuntimeException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "targetId 형식이 올바르지 않습니다.");
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
