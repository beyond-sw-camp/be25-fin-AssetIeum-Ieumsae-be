package com.ieumsae.assetieum.domain.inspection.inspection.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InspectionTargetResolver {

    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;

    public void validateCategory(InspectionType inspectionType, UUID companyId, UUID categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "조사 대상 카테고리가 필요합니다.");
        }

        switch (inspectionType) {
            case TANGIBLE_ASSET -> tangibleAssetCategoryRepository.findByIdAndCompany_Id(categoryId, companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
            case INTANGIBLE_ASSET -> intangibleAssetCategoryRepository.findByIdAndCompany_Id(categoryId, companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }
    }

    public InspectionTarget createTarget(
            Company company,
            Inspection inspection,
            InspectionType inspectionType,
            UUID assetId
    ) {
        if (assetId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "조사 대상 자산이 필요합니다.");
        }

        return switch (inspectionType) {
            case TANGIBLE_ASSET -> createTangibleTarget(company, inspection, assetId);
            case INTANGIBLE_ASSET -> createIntangibleTarget(company, inspection, assetId);
        };
    }

    public List<InspectionTarget> createTargets(
            Company company,
            Inspection inspection,
            InspectionType inspectionType,
            InspectionTargetType targetType,
            Department department,
            UUID categoryId
    ) {
        return switch (inspectionType) {
            case TANGIBLE_ASSET -> createTangibleTargets(company, inspection, targetType, department, categoryId);
            case INTANGIBLE_ASSET -> createIntangibleTargets(company, inspection, targetType, department, categoryId);
        };
    }

    private InspectionTarget createTangibleTarget(Company company, Inspection inspection, UUID assetId) {
        TangibleAsset asset = tangibleAssetRepository.findByIdAndCompany_Id(assetId, company.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        return InspectionTarget.builder()
                .company(company)
                .inspection(inspection)
                .tangibleAsset(asset)
                .build();
    }

    private InspectionTarget createIntangibleTarget(Company company, Inspection inspection, UUID assetId) {
        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, company.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        return InspectionTarget.builder()
                .company(company)
                .inspection(inspection)
                .intangibleAsset(asset)
                .build();
    }

    private List<InspectionTarget> createTangibleTargets(
            Company company,
            Inspection inspection,
            InspectionTargetType targetType,
            Department department,
            UUID categoryId
    ) {
        List<TangibleAsset> assets = switch (targetType) {
            case ALL -> tangibleAssetRepository.findAllByCompany_IdAndTangibleAssetStatus(
                    company.getId(),
                    TangibleAssetStatus.IN_USE
            );
            case DEPARTMENT -> tangibleAssetRepository.findAllByCompany_IdAndDepartment_IdInAndTangibleAssetStatus(
                    company.getId(),
                    resolveDepartmentAndDescendantIds(department.getId(), company.getId()),
                    TangibleAssetStatus.IN_USE
            );
            case CATEGORY -> tangibleAssetRepository.findAllByCompany_IdAndTangibleAssetItem_TangibleAssetCategory_IdInAndTangibleAssetStatus(
                    company.getId(),
                    resolveTangibleCategoryIds(categoryId, company.getId()),
                    TangibleAssetStatus.IN_USE
            );
        };

        return assets.stream()
                .map(asset -> InspectionTarget.builder()
                        .company(company)
                        .inspection(inspection)
                        .tangibleAsset(asset)
                        .build())
                .toList();
    }

    private List<InspectionTarget> createIntangibleTargets(
            Company company,
            Inspection inspection,
            InspectionTargetType targetType,
            Department department,
            UUID categoryId
    ) {
        List<IntangibleAsset> assets = switch (targetType) {
            case ALL -> intangibleAssetRepository.findAllByCompany_IdAndIntangibleAssetStatus(
                    company.getId(),
                    IntangibleAssetStatus.IN_USE
            );
            case DEPARTMENT -> intangibleAssetRepository.findAllByCompany_IdAndDepartment_IdInAndIntangibleAssetStatus(
                    company.getId(),
                    resolveDepartmentAndDescendantIds(department.getId(), company.getId()),
                    IntangibleAssetStatus.IN_USE
            );
            case CATEGORY -> intangibleAssetRepository.findAllByCompany_IdAndIntangibleAssetItem_IntangibleAssetCategory_IdInAndIntangibleAssetStatus(
                    company.getId(),
                    resolveIntangibleCategoryIds(categoryId, company.getId()),
                    IntangibleAssetStatus.IN_USE
            );
        };

        return assets.stream()
                .map(asset -> InspectionTarget.builder()
                        .company(company)
                        .inspection(inspection)
                        .intangibleAsset(asset)
                        .build())
                .toList();
    }

    private List<UUID> resolveTangibleCategoryIds(UUID categoryId, UUID companyId) {
        List<UUID> categoryIds = new ArrayList<>(
                tangibleAssetCategoryRepository.findAllDescendantIds(categoryId, companyId)
        );
        categoryIds.add(categoryId);
        return categoryIds;
    }

    private List<UUID> resolveIntangibleCategoryIds(UUID categoryId, UUID companyId) {
        List<UUID> categoryIds = new ArrayList<>(
                intangibleAssetCategoryRepository.findAllDescendantIds(categoryId, companyId)
        );
        categoryIds.add(categoryId);
        return categoryIds;
    }

    private List<UUID> resolveDepartmentAndDescendantIds(UUID departmentId, UUID companyId) {
        List<Department> departments = departmentRepository.findAllByCompany_IdAndDeletedAtIsNullOrderByCreatedAtAsc(
                companyId
        );
        Set<UUID> departmentIds = new LinkedHashSet<>();
        departmentIds.add(departmentId);

        boolean added;
        do {
            added = false;
            for (Department department : departments) {
                Department parentDepartment = department.getParentDepartment();
                if (parentDepartment != null
                        && departmentIds.contains(parentDepartment.getId())
                        && departmentIds.add(department.getId())) {
                    added = true;
                }
            }
        } while (added);

        return new ArrayList<>(departmentIds);
    }
}
