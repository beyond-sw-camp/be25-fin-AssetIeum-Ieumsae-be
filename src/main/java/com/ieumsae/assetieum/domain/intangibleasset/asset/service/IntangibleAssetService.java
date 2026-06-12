package com.ieumsae.assetieum.domain.intangibleasset.asset.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetService {

    private static final String INTANGIBLE_ASSET_CODE_PREFIX = "IA";
    private static final String REDIS_KEY_PREFIX = "intangible-asset:code:";

    private final CompanyRepository companyRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;

    private final CodeGenerator codeGenerator;


    /**
     * 무형자산 등록.
     * 회사, 품목, 라이선스 코드 중복 여부를 검증하고 자산을 생성한다.
     */
    @Transactional
    public IntangibleAssetResponse createAsset(IntangibleAssetCreateRequest request, UUID companyId) {

        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                request.getIntangibleItemId(),
                companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(request.getLicenseCode() != null) {
            if(intangibleAssetRepository.existsByCompany_IdAndLicenseCodeAndIntangibleAssetItem_Id(
                    companyId,
                    request.getLicenseCode(),
                    request.getIntangibleItemId()
            )) {
                throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
            }
        }

        IntangibleAssetStatus status = resolveCreateStatus(request);
        Department department = findDepartment(request.getDepartmentId(), companyId);
        Member member = findMember(request.getMemberId(), companyId);

        validateMemberDepartment(member, department);
        validateRequiredUsageInfo(status, member, department, request.getStartedAt());
        validateCreateExpiredAt(request);


        // 2. 무형자산 생성 및 저장
        IntangibleAsset asset = IntangibleAsset.builder()
                .company(company)
                .intangibleAssetItem(item)
                .licenseCode(request.getLicenseCode())
                .intangibleAssetStatus(status)
                .seatCount(request.getSeatCount())
                .member(member)
                .department(department)
                .assetCode(codeGenerator.generate(INTANGIBLE_ASSET_CODE_PREFIX, REDIS_KEY_PREFIX))
                .startedAt(request.getStartedAt())
                .expiredAt(request.getExpiredAt())
                .isAutoRenewal(request.getIsAutoRenewal())
                .billingCycle(request.getBillingCycle())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseVendor(request.getPurchaseVendor())
                .build();

        IntangibleAsset savedAsset = intangibleAssetRepository.save(asset);

        return IntangibleAssetResponse.from(savedAsset);
    }
    private Department findDepartment(UUID departmentId, UUID companyId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(departmentId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private Member findMember(UUID memberId, UUID companyId) {
        if (memberId == null) {
            return null;
        }

        return memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private IntangibleAssetStatus resolveCreateStatus(IntangibleAssetCreateRequest request) {
        if (request.getIntangibleAssetStatus() == null) {
            return IntangibleAssetStatus.AVAILABLE;
        }
        return request.getIntangibleAssetStatus();
    }

    /**
     * 사용자와 부서가 함께 지정된 경우 사용자의 소속 부서와 지정 부서가 일치하는지 검증한다.
     */
    private void validateMemberDepartment(Member member, Department department) {
        if (member == null || department == null) {
            return;
        }

        if (!member.getDepartment().getId().equals(department.getId())) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "사용자의 소속 부서와 자산에 등록되는 부서가 일치하지 않습니다."
            );
        }
    }

    /**
     * AVAILABLE, DISPOSED를 제외한 상태에서는 사용자, 부서, 사용 시작일이 모두 존재하는지 검증한다.
     */
    private void validateRequiredUsageInfo(
            IntangibleAssetStatus status,
            Member member,
            Department department,
            LocalDateTime startedAt
    ) {
        if (status == IntangibleAssetStatus.AVAILABLE || status == IntangibleAssetStatus.EXPIRED || status == IntangibleAssetStatus.CANCELED) {
            return;
        }

        if (member == null || department == null || startedAt == null) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "AVAILABLE, EXPIRED, CANCELED 상태가 아닌 자산은 사용자, 부서, 사용 시작일이 필요합니다."
            );
        }
    }

    /**
     * 등록 시 만료일 입력 조건을 검증한다.
     */
    private void validateCreateExpiredAt(IntangibleAssetCreateRequest request) {
        if(request.getExpiredAt() == null) {
            return;
        }

        if(request.getBillingCycle() == BillingCycle.ONE_TIME){
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "영구 사용 자산은 만료일을 입력할 수 없습니다."
            );
        }

        if (request.getStartedAt().isAfter(request.getExpiredAt())) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "시작일은 만료일보다 늦을 수 없습니다."
            );
        }
    }



}
