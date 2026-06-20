package com.ieumsae.assetieum.domain.inspection.inspection.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionCreateRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.repository.InspectionRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    @Transactional
    public InspectionResponse createInspection(
            InspectionCreateRequest request,
            InspectionType inspectionType,
            UUID companyId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member inspector = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getInspectorId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = resolveTargetDepartment(request, companyId);
        validateTargetCategory(request, inspectionType, companyId);

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
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "조사 대상 부서가 필요합니다.");
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

    public PaginationResponse<InspectionSearchResponse> getInspections(
            InspectionSearchRequest request,
            InspectionType inspectionType,
            UUID companyId
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (request.getInspectorId() != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getInspectorId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

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
            case ALL -> "전사";
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
}
