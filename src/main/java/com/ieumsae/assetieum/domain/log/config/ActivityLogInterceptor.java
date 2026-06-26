package com.ieumsae.assetieum.domain.log.config;

import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.domain.log.service.LogTargetPathService;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ActivityLogInterceptor implements HandlerInterceptor {

	private static final Pattern UUID_PATTERN = Pattern.compile(
		"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
	);

	private final LogService logService;
	private final LogTargetPathService logTargetPathService;

	@Override
	public void afterCompletion(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler,
		Exception ex
	) {
		if (response.getStatus() >= 400) {
			return;
		}

		AuthenticatedMember member = resolveAuthenticatedMember();
		if (member == null) {
			return;
		}

		String uri = request.getRequestURI();
		if (uri.contains("/auth/")) {
			return;
		}
		if (!"GET".equalsIgnoreCase(request.getMethod())) {
			recordAuditLog(request, member, uri);
			return;
		}

		ActivityLogAction action = request.getQueryString() == null
			? ActivityLogAction.VIEW
			: ActivityLogAction.SEARCH;
		logService.recordActivityLog(
			member,
			action,
			resolveSubjectType(uri),
			resolveSubjectId(uri),
			logTargetPathService.resolve(uri, member.companyId()),
			createDetail(action, uri, request.getQueryString())
		);
	}

	private void recordAuditLog(HttpServletRequest request, AuthenticatedMember member, String uri) {
		AuditLogAction action = resolveAuditAction(request.getMethod(), uri);
		logService.recordAuditLog(
			member,
			action,
			resolveSubjectType(uri),
			resolveSubjectId(uri),
			logTargetPathService.resolve(uri, member.companyId()),
			createAuditDetail(action, request.getMethod(), uri)
		);
	}

	private AuditLogAction resolveAuditAction(String method, String uri) {
		String lowerUri = uri.toLowerCase();
		if (lowerUri.contains("assign")) {
			return AuditLogAction.ASSIGN;
		}
		if (lowerUri.contains("invite")) {
			return AuditLogAction.INVITE;
		}
		if (lowerUri.contains("return")) {
			return AuditLogAction.RETURN;
		}
		if ("DELETE".equalsIgnoreCase(method)) {
			return AuditLogAction.DELETE;
		}
		if ("POST".equalsIgnoreCase(method)) {
			return AuditLogAction.CREATE;
		}
		return AuditLogAction.INFORMATION_CHANGE;
	}

	private AuthenticatedMember resolveAuthenticatedMember() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedMember member)) {
			return null;
		}
		return member;
	}

	private LogSubjectType resolveSubjectType(String uri) {
		if (uri.contains("/tangible-asset/items")) {
			return LogSubjectType.TANGIBLE_ASSET_ITEM;
		}
		if (uri.contains("/intangible-asset/items")) {
			return LogSubjectType.INTANGIBLE_ASSET_ITEM;
		}
		if (uri.contains("/tangible-asset/assets") || uri.contains("/tangible-assets")) {
			return LogSubjectType.TANGIBLE_ASSET;
		}
		if (uri.contains("/intangible-asset/assets") || uri.contains("/intangible-assets")) {
			return LogSubjectType.INTANGIBLE_ASSET;
		}
		if (uri.contains("/members")) {
			return LogSubjectType.MEMBER;
		}
		if (uri.contains("/tickets")) {
			return LogSubjectType.TICKET;
		}
		if (uri.contains("/purchase-plans")) {
			return LogSubjectType.PURCHASE_PLAN;
		}
		if (uri.contains("/budgets") || uri.contains("/budget-histories")) {
			return LogSubjectType.BUDGET;
		}
		if (uri.contains("/departments")) {
			return LogSubjectType.DEPARTMENT;
		}
		if (uri.contains("/inspections")) {
			return LogSubjectType.INSPECTION;
		}
		if (uri.contains("/hr-")) {
			return LogSubjectType.HR_EVENT;
		}
		return LogSubjectType.SYSTEM;
	}

	private UUID resolveSubjectId(String uri) {
		Matcher matcher = UUID_PATTERN.matcher(uri);
		if (!matcher.find()) {
			return null;
		}
		return UUID.fromString(matcher.group());
	}

	private String createDetail(ActivityLogAction action, String uri, String queryString) {
		if (action == ActivityLogAction.SEARCH) {
			return "Searched " + uri + "?" + queryString;
		}
		return "Viewed " + uri;
	}

	private String createAuditDetail(AuditLogAction action, String method, String uri) {
		return action.name() + " by " + method.toUpperCase() + " " + uri;
	}
}
