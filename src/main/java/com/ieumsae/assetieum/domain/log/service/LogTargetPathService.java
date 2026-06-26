package com.ieumsae.assetieum.domain.log.service;

import com.ieumsae.assetieum.domain.inspection.followup.repository.InspectionFollowUpRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LogTargetPathService {

	private static final String API_PREFIX = "/api/v1";
	private static final Pattern UUID_PATTERN = Pattern.compile(
		"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
	);

	private final InspectionTargetRepository inspectionTargetRepository;
	private final InspectionFollowUpRepository inspectionFollowUpRepository;

	public String resolve(String uri, UUID companyId) {
		if (!StringUtils.hasText(uri)) {
			return null;
		}
		if (uri.contains("/auth/")) {
			return null;
		}
		if (uri.contains("/inspections/targets/") && uri.contains("/result")) {
			return resolveInspectionTargetPath(uri, companyId);
		}
		if (uri.contains("/inspections/follow-ups/")) {
			return resolveInspectionFollowUpPath(uri, companyId);
		}
		if (uri.contains("/tangible-asset/inspections")) {
			return normalizeInspectionPath(uri, "/tangible-asset/inspections");
		}
		if (uri.contains("/intangible-asset/inspections")) {
			return normalizeInspectionPath(uri, "/intangible-asset/inspections");
		}
		return normalizeCommonPath(uri);
	}

	private String resolveInspectionTargetPath(String uri, UUID companyId) {
		UUID targetId = resolveFirstUuid(uri);
		if (targetId == null) {
			return "/inspections";
		}
		return inspectionTargetRepository.findByIdAndCompany_Id(targetId, companyId)
			.map(target -> toInspectionPath(target.getInspection()))
			.orElse("/inspections");
	}

	private String resolveInspectionFollowUpPath(String uri, UUID companyId) {
		UUID followUpId = resolveFirstUuid(uri);
		if (followUpId == null) {
			return "/inspections";
		}
		return inspectionFollowUpRepository.findByIdAndCompany_Id(followUpId, companyId)
			.map(followUp -> toInspectionPath(followUp.getInspectionResult().getInspection()))
			.orElse("/inspections");
	}

	private String toInspectionPath(Inspection inspection) {
		String basePath = inspection.getInspectionType() == InspectionType.TANGIBLE_ASSET
			? "/tangible-asset/inspections"
			: "/intangible-asset/inspections";
		return basePath + "/" + inspection.getId();
	}

	private String normalizeInspectionPath(String uri, String basePath) {
		UUID inspectionId = resolveFirstUuid(uri);
		if (inspectionId == null) {
			return basePath;
		}
		return basePath + "/" + inspectionId;
	}

	private String normalizeCommonPath(String uri) {
		String path = stripApiPrefix(uri);
		UUID id = resolveFirstUuid(uri);
		if (path.contains("/tickets")) {
			return id == null ? "/tickets" : "/tickets/" + id;
		}
		if (path.contains("/members")) {
			return id == null ? "/members" : "/members/" + id;
		}
		if (path.contains("/departments")) {
			return id == null ? "/departments" : "/departments/" + id;
		}
		if (path.contains("/purchase-plans")) {
			return id == null ? "/purchase-plans" : "/purchase-plans/" + id;
		}
		if (path.contains("/budgets") || path.contains("/budget-histories")) {
			return id == null ? "/budgets" : "/budgets/" + id;
		}
		if (path.contains("/tangible-asset/items")) {
			return id == null ? "/tangible-asset/items" : "/tangible-asset/items/" + id;
		}
		if (path.contains("/intangible-asset/items")) {
			return id == null ? "/intangible-asset/items" : "/intangible-asset/items/" + id;
		}
		if (path.contains("/tangible-asset/assets") || path.contains("/tangible-assets")) {
			return id == null ? "/tangible-asset/assets" : "/tangible-asset/assets/" + id;
		}
		if (path.contains("/intangible-asset/assets") || path.contains("/intangible-assets")) {
			return id == null ? "/intangible-asset/assets" : "/intangible-asset/assets/" + id;
		}
		if (path.contains("/hr-")) {
			return id == null ? "/hr-events" : "/hr-events/" + id;
		}
		if (path.contains("/assignable-items")) {
			return parentPath(path, "/assignable-items");
		}
		if (path.contains("/assignable-assets")) {
			return parentPath(path, "/assignable-assets");
		}
		if (path.contains("/direct-purchase-result")) {
			return parentPath(path, "/direct-purchase-result");
		}
		if (path.contains("/direct-purchase-assets")) {
			return parentPath(path, "/direct-purchase-assets");
		}
		if (path.contains("/comments")) {
			return parentPath(path, "/comments");
		}
		if (path.contains("/status")) {
			return parentPath(path, "/status");
		}
		if (path.contains("/complete")) {
			return parentPath(path, "/complete");
		}
		if (path.contains("/cancel")) {
			return parentPath(path, "/cancel");
		}
		if (path.contains("/approve")) {
			return parentPath(path, "/approve");
		}
		if (path.contains("/reject")) {
			return parentPath(path, "/reject");
		}
		return path;
	}

	private String parentPath(String path, String marker) {
		int markerIndex = path.indexOf(marker);
		if (markerIndex <= 0) {
			return path;
		}
		return path.substring(0, markerIndex);
	}

	private String stripApiPrefix(String uri) {
		if (uri.startsWith(API_PREFIX)) {
			return uri.substring(API_PREFIX.length());
		}
		return uri;
	}

	private UUID resolveFirstUuid(String uri) {
		Matcher matcher = UUID_PATTERN.matcher(uri);
		if (!matcher.find()) {
			return null;
		}
		return UUID.fromString(matcher.group());
	}
}
