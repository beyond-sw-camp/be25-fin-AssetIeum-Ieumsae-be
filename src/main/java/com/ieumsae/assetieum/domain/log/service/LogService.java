package com.ieumsae.assetieum.domain.log.service;

import com.ieumsae.assetieum.domain.log.dto.ActivityLogResponse;
import com.ieumsae.assetieum.domain.log.dto.ActivityLogSearchRequest;
import com.ieumsae.assetieum.domain.log.dto.AuditLogResponse;
import com.ieumsae.assetieum.domain.log.dto.AuditLogSearchRequest;
import com.ieumsae.assetieum.domain.log.entity.ActivityLog;
import com.ieumsae.assetieum.domain.log.entity.AuditLog;
import com.ieumsae.assetieum.domain.log.event.ActivityLogEvent;
import com.ieumsae.assetieum.domain.log.event.AuditLogEvent;
import com.ieumsae.assetieum.domain.log.event.LogEventPublisher;
import com.ieumsae.assetieum.domain.log.repository.ActivityLogRepository;
import com.ieumsae.assetieum.domain.log.repository.AuditLogRepository;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService {

	private final AuditLogRepository auditLogRepository;
	private final ActivityLogRepository activityLogRepository;
	private final MemberRepository memberRepository;
	private final LogEventPublisher logEventPublisher;

	@Value("${app.kafka.log.enabled:false}")
	private boolean kafkaLogEnabled;

	public PaginationResponse<AuditLogResponse> getAuditLogs(
		AuditLogSearchRequest request,
		UUID companyId
	) {
		return PaginationResponse.from(
			auditLogRepository.findAll(auditSpecification(request, companyId), request.toPageable())
				.map(AuditLogResponse::from)
		);
	}

	public PaginationResponse<ActivityLogResponse> getActivityLogs(
		ActivityLogSearchRequest request,
		UUID companyId
	) {
		return PaginationResponse.from(
			activityLogRepository.findAll(activitySpecification(request, companyId), request.toPageable())
				.map(ActivityLogResponse::from)
		);
	}

	@Transactional
	public void recordAuditLog(
		AuthenticatedMember authenticatedMember,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String detail
	) {
		recordAuditLog(authenticatedMember, action, subjectType, subjectId, null, detail);
	}

	@Transactional
	public void recordAuditLog(
		AuthenticatedMember authenticatedMember,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		if (kafkaLogEnabled) {
			logEventPublisher.publishAuditLog(
				authenticatedMember.companyId(),
				authenticatedMember.id(),
				action,
				subjectType,
				subjectId,
				targetPath,
				detail
			);
			return;
		}
		Member actor = findMember(authenticatedMember.id(), authenticatedMember.companyId());
		saveAuditLog(null, actor, action, subjectType, subjectId, targetPath, detail);
	}

	@Transactional
	public void recordAuditLog(
		Member actor,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String detail
	) {
		recordAuditLog(actor, action, subjectType, subjectId, null, detail);
	}

	@Transactional
	public void recordAuditLog(
		Member actor,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		if (kafkaLogEnabled) {
			logEventPublisher.publishAuditLog(
				actor.getCompany().getId(),
				actor.getId(),
				action,
				subjectType,
				subjectId,
				targetPath,
				detail
			);
			return;
		}
		saveAuditLog(null, actor, action, subjectType, subjectId, targetPath, detail);
	}

	@Transactional
	public void recordActivityLog(
		AuthenticatedMember authenticatedMember,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String detail
	) {
		recordActivityLog(authenticatedMember, action, subjectType, subjectId, null, detail);
	}

	@Transactional
	public void recordActivityLog(
		AuthenticatedMember authenticatedMember,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		if (kafkaLogEnabled) {
			logEventPublisher.publishActivityLog(
				authenticatedMember.companyId(),
				authenticatedMember.id(),
				action,
				subjectType,
				subjectId,
				targetPath
			);
			return;
		}
		Member actor = findMember(authenticatedMember.id(), authenticatedMember.companyId());
		saveActivityLog(null, actor, action, subjectType, subjectId, targetPath);
	}

	@Transactional
	public void recordActivityLog(
		Member actor,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String detail
	) {
		recordActivityLog(actor, action, subjectType, subjectId, null, detail);
	}

	@Transactional
	public void recordActivityLog(
		Member actor,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		if (kafkaLogEnabled) {
			logEventPublisher.publishActivityLog(
				actor.getCompany().getId(),
				actor.getId(),
				action,
				subjectType,
				subjectId,
				targetPath
			);
			return;
		}
		saveActivityLog(null, actor, action, subjectType, subjectId, targetPath);
	}

	@Transactional
	public void persistAuditLogEvent(UUID eventId, UUID companyId, AuditLogEvent event) {
		if (auditLogRepository.existsByEventId(eventId)) {
			return;
		}
		Member actor = findMember(event.memberId(), companyId);
		saveAuditLog(
			eventId,
			actor,
			event.action(),
			event.subjectType(),
			event.subjectId(),
			event.targetPath(),
			event.detail()
		);
	}

	@Transactional
	public void persistActivityLogEvent(UUID eventId, UUID companyId, ActivityLogEvent event) {
		if (activityLogRepository.existsByEventId(eventId)) {
			return;
		}
		Member actor = findMember(event.memberId(), companyId);
		saveActivityLog(
			eventId,
			actor,
			event.action(),
			event.subjectType(),
			event.subjectId(),
			event.targetPath()
		);
	}

	private void saveAuditLog(
		UUID eventId,
		Member actor,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		auditLogRepository.save(AuditLog.builder()
			.eventId(eventId)
			.company(actor.getCompany())
			.member(actor)
			.action(action)
			.subjectType(subjectType)
			.subjectId(resolveSubjectId(subjectId))
			.targetPath(normalizeTargetPath(targetPath))
			.beforeValue("-")
			.afterValue(normalizeDetail(detail))
			.build());
	}

	private void saveActivityLog(
		UUID eventId,
		Member actor,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath
	) {
		activityLogRepository.save(ActivityLog.builder()
			.eventId(eventId)
			.company(actor.getCompany())
			.member(actor)
			.action(action)
			.subjectType(subjectType)
			.subjectId(resolveSubjectId(subjectId))
			.targetPath(normalizeTargetPath(targetPath))
			.build());
	}

	private Specification<AuditLog> auditSpecification(AuditLogSearchRequest request, UUID companyId) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("company").get("id"), companyId));
			if (request.getAction() != null) {
				predicates.add(cb.equal(root.get("action"), request.getAction()));
			}
			if (request.getSubjectType() != null) {
				predicates.add(cb.equal(root.get("subjectType"), request.getSubjectType()));
			}
			if (request.getSubjectId() != null) {
				predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
			}
			addAuditKeywordPredicates(request.getKeyword(), predicates, root, cb);
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private Specification<ActivityLog> activitySpecification(ActivityLogSearchRequest request, UUID companyId) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("company").get("id"), companyId));
			if (request.getAction() != null) {
				predicates.add(cb.equal(root.get("action"), request.getAction()));
			}
			if (request.getSubjectType() != null) {
				predicates.add(cb.equal(root.get("subjectType"), request.getSubjectType()));
			}
			if (request.getSubjectId() != null) {
				predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
			}
			addActivityKeywordPredicates(request.getKeyword(), predicates, root, cb);
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private void addAuditKeywordPredicates(
		String keyword,
		List<Predicate> predicates,
		jakarta.persistence.criteria.Root<AuditLog> root,
		jakarta.persistence.criteria.CriteriaBuilder cb
	) {
		if (!StringUtils.hasText(keyword)) {
			return;
		}
		String pattern = "%" + keyword.trim().toLowerCase() + "%";
		predicates.add(cb.or(
			cb.like(cb.lower(root.get("member").get("name")), pattern),
			cb.like(cb.lower(root.get("member").get("memberNo")), pattern),
			cb.like(cb.lower(root.get("action").as(String.class)), pattern),
			cb.like(cb.lower(root.get("subjectType").as(String.class)), pattern),
			cb.like(cb.lower(root.get("beforeValue")), pattern),
			cb.like(cb.lower(root.get("afterValue")), pattern)
		));
	}

	private void addActivityKeywordPredicates(
		String keyword,
		List<Predicate> predicates,
		jakarta.persistence.criteria.Root<ActivityLog> root,
		jakarta.persistence.criteria.CriteriaBuilder cb
	) {
		if (!StringUtils.hasText(keyword)) {
			return;
		}
		String pattern = "%" + keyword.trim().toLowerCase() + "%";
		predicates.add(cb.or(
			cb.like(cb.lower(root.get("member").get("name")), pattern),
			cb.like(cb.lower(root.get("member").get("memberNo")), pattern),
			cb.like(cb.lower(root.get("action").as(String.class)), pattern),
			cb.like(cb.lower(root.get("subjectType").as(String.class)), pattern)
		));
	}

	private Member findMember(UUID memberId, UUID companyId) {
		return memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private String normalizeDetail(String detail) {
		if (!StringUtils.hasText(detail)) {
			return "-";
		}
		return detail.trim();
	}

	private String normalizeTargetPath(String targetPath) {
		if (!StringUtils.hasText(targetPath)) {
			return null;
		}
		return targetPath.trim();
	}

	private UUID resolveSubjectId(UUID subjectId) {
		return subjectId == null ? new UUID(0L, 0L) : subjectId;
	}
}
