package com.ieumsae.assetieum.domain.inspection.inspection.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionCreateRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionDetailResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.repository.InspectionRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionTargetResolver inspectionTargetResolver;
    private final InspectionTargetRepository inspectionTargetRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;

    /**
     * 전수조사 계획 등록
     * inspectionType에 따라 유형/무형을 구분한다.
     */
    @Transactional
    public InspectionResponse createInspection(
            InspectionCreateRequest request,
            InspectionType inspectionType,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member inspector = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getInspectorId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = resolveTargetDepartment(request, companyId);
        validateTargetCategory(request, inspectionType, companyId);

        // 2. 전수조사 등록
        Inspection inspection = Inspection.builder()
                .company(company)
                .inspectionType(inspectionType)
                .targetType(request.getTargetType())
                .targetDepartment(department)
                .targetCategoryId(request.getTargetCategoryId())
                .inspectorType(request.getInspectorType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .inspector(inspector)
                .build();

        Inspection savedInspection = inspectionRepository.save(inspection);

        // 3. 전수조사 유형에 따라 해당하는 타겟 리스트를 생성하고 저장
        List<InspectionTarget> targets = inspectionTargetResolver.createTargets(
                company,
                savedInspection,
                inspectionType,
                request.getTargetType(),
                department,
                request.getTargetCategoryId()
        );
        inspectionTargetRepository.saveAll(targets);

        return InspectionResponse.from(savedInspection);
    }

    private Department resolveTargetDepartment(InspectionCreateRequest request, UUID companyId) {
        if (request.getTargetType() != InspectionTargetType.DEPARTMENT) {
            return null;
        }

        if (request.getTargetDepartmentId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Target department is required.");
        }

        return departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getTargetDepartmentId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private void validateTargetCategory(
            InspectionCreateRequest request,
            InspectionType inspectionType,
            UUID companyId
    ) {
        if (request.getTargetType() != InspectionTargetType.CATEGORY) {
            return;
        }

        inspectionTargetResolver.validateCategory(inspectionType, companyId, request.getTargetCategoryId());
    }


    /**
     * 전주조사 목록 조회
     * 상태, 조사자 ID를 필터링하여 페이징 처리된 목록을 반환
     */
    public PaginationResponse<InspectionSearchResponse> getInspections(
            InspectionSearchRequest request,
            InspectionType inspectionType,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (request.getInspectorId() != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getInspectorId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        // 2. 전수조사 목록 페이징 처리 및 반환
        Page<InspectionSearchResponse> inspections = inspectionRepository.search(
                        companyId,
                        inspectionType,
                        request.getStatus(),
                        request.getInspectorId(),
                        request.toPageable()
                )
                .map(inspection -> InspectionSearchResponse.from(
                        inspection,
                        resolveTargetName(inspection, inspectionType, companyId)
                ));

        return PaginationResponse.from(inspections);
    }

    private String resolveTargetName(
            Inspection inspection,
            InspectionType inspectionType,
            UUID companyId
    ) {
        return switch (inspection.getTargetType()) {
            case ALL -> "All";
            case DEPARTMENT -> inspection.getTargetDepartment() != null
                    ? inspection.getTargetDepartment().getName()
                    : null;
            case CATEGORY -> resolveCategoryName(inspection, inspectionType, companyId);
        };
    }

    private String resolveCategoryName(
            Inspection inspection,
            InspectionType inspectionType,
            UUID companyId
    ) {
        UUID categoryId = inspection.getTargetCategoryId();
        if (categoryId == null) {
            return null;
        }

        if (inspectionType == InspectionType.TANGIBLE_ASSET) {
            return tangibleAssetCategoryRepository.findByIdAndCompany_Id(categoryId, companyId)
                    .map(category -> category.getName())
                    .orElse(null);
        }

        return intangibleAssetCategoryRepository.findByIdAndCompany_Id(categoryId, companyId)
                .map(category -> category.getName())
                .orElse(null);
    }

    /**
     * 전수조사 상세 조회
     * 전수조사의 유형/무형에 따른 상세 정보를 조회한다.
     */
    public InspectionDetailResponse getInspectionDetail(
            UUID inspectionId,
            InspectionType inspectionType,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Inspection inspection = inspectionRepository.findDetailByIdAndCompanyIdAndInspectionType(
                        inspectionId,
                        companyId,
                        inspectionType
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 2. 전수조사 타겟 리스트 조회
        List<InspectionTarget> targets = inspectionRepository.findTargetsWithAssets(inspectionId, companyId);

        // 3. 전수조사 결과 리스트 조회
        Map<UUID, InspectionResult> resultByTargetId = inspectionRepository
                .findResults(inspectionId, companyId)
                .stream()
                .collect(Collectors.toMap(
                        result -> result.getInspectionTarget().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<InspectionDetailResponse.InspectionResultItem> inspectionResults = targets.stream()
                .filter(target -> Boolean.TRUE.equals(target.getIsResponded()))
                .map(target -> toInspectionResultItem(
                        target,
                        resultByTargetId.get(target.getId())
                ))
                .toList();

        // 4. 미점검 자산 리스트 조회
        List<InspectionDetailResponse.UninspectedAssetItem> uninspectedAssets = targets.stream()
                .filter(target -> !Boolean.TRUE.equals(target.getIsResponded()))
                .map(this::toUninspectedAssetItem)
                .toList();

        // 5. 결과 반환
        return InspectionDetailResponse.of(
                inspection,
                resolveTargetName(inspection, inspectionType, companyId),
                inspectionResults,
                uninspectedAssets
        );
    }

    private InspectionDetailResponse.InspectionResultItem toInspectionResultItem(
            InspectionTarget target,
            InspectionResult result
    ) {
        return InspectionDetailResponse.InspectionResultItem.builder()
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .followUpRequired(result != null && Boolean.TRUE.equals(result.getFollowUpRequests()))
                .userResponseContent(result != null ? result.getResponseContent() : null)
                .build();
    }

    private InspectionDetailResponse.UninspectedAssetItem toUninspectedAssetItem(InspectionTarget target) {
        return InspectionDetailResponse.UninspectedAssetItem.builder()
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .category(resolveAssetCategory(target))
                .build();
    }

    private String resolveProductName(InspectionTarget target) {
        TangibleAsset tangibleAsset = target.getTangibleAsset();
        if (tangibleAsset != null) {
            return tangibleAsset.getTangibleAssetItem().getProductName();
        }

        IntangibleAsset intangibleAsset = target.getIntangibleAsset();
        if (intangibleAsset != null) {
            return intangibleAsset.getIntangibleAssetItem().getProductName();
        }

        return null;
    }

    private String resolveAssetCode(InspectionTarget target) {
        TangibleAsset tangibleAsset = target.getTangibleAsset();
        if (tangibleAsset != null) {
            return tangibleAsset.getAssetCode();
        }

        IntangibleAsset intangibleAsset = target.getIntangibleAsset();
        if (intangibleAsset != null) {
            return intangibleAsset.getAssetCode();
        }

        return null;
    }

    private String resolveAssetCategory(InspectionTarget target) {
        TangibleAsset tangibleAsset = target.getTangibleAsset();
        if (tangibleAsset != null) {
            return tangibleAsset.getTangibleAssetItem().getTangibleAssetCategory().getName();
        }

        IntangibleAsset intangibleAsset = target.getIntangibleAsset();
        if (intangibleAsset != null) {
            return intangibleAsset.getIntangibleAssetItem().getIntangibleAssetCategory().getName();
        }

        return null;
    }
}
