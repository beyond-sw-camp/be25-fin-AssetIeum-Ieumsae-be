package com.ieumsae.assetieum.domain.hr.hrtemplate.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateResponse;
import com.ieumsae.assetieum.domain.hr.hrtemplate.entity.HrTemplate;
import com.ieumsae.assetieum.domain.hr.hrtemplate.repository.HrTemplateRepository;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.dto.HrTemplateItemCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.entity.HrTemplateItem;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.repository.HrTemplateItemRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrTemplateService {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final HrTemplateRepository hrTemplateRepository;
    private final HrTemplateItemRepository hrTemplateItemRepository;
    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final IntangibleAssetItemRepository intangibleAssetItemRepository;

    @Transactional
    public HrTemplateResponse createHrTemplate(
            HrTemplateCreateRequest request,
            AuthenticatedMember member
    ) {

        // 1. 입력값 검증
        Company company = companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member requester = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(member.id(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(requester.getDepartment().getId(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 2. HR 템플릿 등록
        Optional<HrTemplate> existingTemplate = hrTemplateRepository
                .findByCompany_IdAndDepartment_IdAndDeletedAtIsNull(company.getId(), department.getId());

        HrTemplate hrTemplate;
        if (existingTemplate.isPresent()) {
            hrTemplate = existingTemplate.get();
            hrTemplateItemRepository.deleteByHrTemplate(hrTemplate);
        } else {
            hrTemplate = hrTemplateRepository.save(HrTemplate.builder()
                    .company(company)
                    .department(department)
                    .build());
        }

        List<HrTemplateItem> hrTemplateItems = createHrTemplateItems(
                request.getItems(),
                hrTemplate,
                member.companyId()
        );

        List<HrTemplateItem> savedHrTemplateItems = hrTemplateItemRepository.saveAll(hrTemplateItems);

        return HrTemplateResponse.from(hrTemplate, savedHrTemplateItems);

    }

    private List<HrTemplateItem> createHrTemplateItems (
            List<HrTemplateItemCreateRequest> requests,
            HrTemplate hrTemplate,
            UUID companyId
    ) {
        List<HrTemplateItem> hrTemplateItems = new ArrayList<>();

        for(HrTemplateItemCreateRequest request: requests) {
            TangibleAssetItem tangibleAssetItem = null;
            IntangibleAssetItem intangibleAssetItem = null;

            if(request.getAssetType() == AssetType.TANGIBLE) {
                tangibleAssetItem = findTangibleAssetItem(request.getAssetItemId(), companyId);
            } else if(request.getAssetType() == AssetType.INTANGIBLE) {
                intangibleAssetItem = findIntangibleAssetItem(request.getAssetItemId(), companyId);
            } else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            hrTemplateItems.add(HrTemplateItem.builder()
                            .hrTemplate(hrTemplate)
                            .intangibleAssetItem(intangibleAssetItem)
                            .tangibleAssetItem(tangibleAssetItem)
                            .quantity(request.getQuantity())
                    .build());
        }

        return hrTemplateItems;

    }

    private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
        if (itemId == null) {
            return null;
        }

        return tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    private IntangibleAssetItem findIntangibleAssetItem(UUID itemId, UUID companyId) {
        if (itemId == null) {
            return null;
        }

        return intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    public HrTemplateResponse getHrTemplate(AuthenticatedMember member) {
        // 1. 입력값 검증
        companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member requester = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(member.id(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(requester.getDepartment().getId(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 2. HR 템플릿 반환
        Optional<HrTemplate> existingTemplate = hrTemplateRepository
                .findByCompany_IdAndDepartment_IdAndDeletedAtIsNull(member.companyId(), department.getId());

        if (existingTemplate.isEmpty()) {
            return null;
        }

        HrTemplate hrTemplate = existingTemplate.get();
        List<HrTemplateItem> hrTemplateItems = hrTemplateItemRepository.findByHrTemplate(hrTemplate);

        return HrTemplateResponse.from(hrTemplate, hrTemplateItems);
    }
}
