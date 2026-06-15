package com.ieumsae.assetieum.domain.tangibleasset.asset.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetService {

    private static final String TANGIBLE_ASSET_CODE_PREFIX = "TA";
    private static final String REDIS_KEY_PREFIX = "tangible-asset:code:";
    private static final Set<TangibleAssetStatus> TICKET_ONLY_UPDATE_STATUSES = EnumSet.of(
            TangibleAssetStatus.IN_USE,
            TangibleAssetStatus.RETURN_REQUESTED,
            TangibleAssetStatus.REPAIR_REQUESTED
    );

    private final TangibleAssetRepository tangibleAssetRepository;
    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;
    private final CodeGenerator codeGenerator;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;

    /**
     * 유형자산 등록.
     * 회사, 품목, 시리얼 번호 중복 여부를 검증하고 자산을 생성한다.
     */
    @Transactional
    public TangibleAssetResponse createAsset(TangibleAssetCreateRequest request, UUID companyId) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        request.getTangibleItemId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        if (tangibleAssetRepository.existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(
                companyId,
                request.getSerialNumber(),
                request.getTangibleItemId()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
        }

        Member member = findMember(request.getMemberId(), companyId);
        Department department = resolveDepartment(request.getDepartmentId(), member, companyId);
        LocalDateTime usedStartedAt = member != null && request.getUsedStartedAt() == null
                ? LocalDateTime.now()
                : request.getUsedStartedAt();
        TangibleAssetStatus status = member != null ? TangibleAssetStatus.IN_USE : resolveCreateStatus(request);

        validateMemberDepartment(member, department);
        validateRequiredUsageInfo(status, member, department, usedStartedAt);
        validateReturnDueDate(request.getReturnDueDate(), request.getUsageType(), usedStartedAt);

        // 2. 유형자산 생성 및 저장
        TangibleAsset asset = TangibleAsset.builder()
                .company(company)
                .tangibleAssetItem(item)
                .usageType(request.getUsageType())
                .assetUsageType(request.getAssetUsageType())
                .tangibleAssetStatus(status)
                .member(member)
                .department(department)
                .assetCode(codeGenerator.generate(TANGIBLE_ASSET_CODE_PREFIX, REDIS_KEY_PREFIX))
                .serialNumber(request.getSerialNumber())
                .location(request.getLocation())
                .usedStartedAt(usedStartedAt)
                .returnDueDate(request.getReturnDueDate())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseVendor(request.getPurchaseVendor())
                .warrantyExpiredAt(request.getWarrantyExpiredAt())
                .build();

        TangibleAsset savedAsset = tangibleAssetRepository.save(asset);
        createAssignmentIfAssigned(company, savedAsset, member, department, usedStartedAt, request);

        return TangibleAssetResponse.from(savedAsset);
    }

    /**
     * 회사 기준 유형자산 목록 조회.
     * 카테고리, 상태, 키워드, 현재 사용자, 부서 조건을 기준으로 필터링한다.
     */
    public PaginationResponse<TangibleAssetSearchResponse> getAssets(
            TangibleAssetSearchRequest request,
            UUID companyId) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (request.getCategoryId() != null) {
            tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }

        if (request.getCurrentUserId() != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getCurrentUserId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        if (request.getDepartmentId() != null) {
            departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getDepartmentId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        // 2. 페이징 처리 및 필터링 후 자산 목록 반환
        Page<TangibleAssetSearchResponse> assetPage = tangibleAssetRepository.search(
                companyId,
                request.getCategoryId(),
                request.getStatus(),
                request.getKeyword(),
                request.getCurrentUserId(),
                request.getDepartmentId(),
                request.toPageable()
        );

        return PaginationResponse.from(assetPage);
    }

    /**
     * 유형자산 상세 조회.
     * 회사 범위 내에서 자산 ID에 해당하는 유형자산 상세 정보를 조회한다.
     */
    public TangibleAssetDetailResponse getAssetDetail(UUID assetId, UUID companyId) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 유형자산 상세 조회
        return tangibleAssetRepository.findDetailByIdAndCompanyId(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    /**
     * 유형자산 수정.
     * 요청값과 기존 자산 값을 조합한 최종 상태 기준으로 사용 정보 필수 여부를 검증한다.
     */
    @Transactional
    public TangibleAssetResponse updateAsset(UUID assetId, TangibleAssetUpdateRequest request, UUID companyId) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAsset asset = tangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        validateUpdateDoesNotChangeAssignment(request);
        validateDirectStatusUpdate(asset, request.getTangibleAssetStatus());

        // 2. 유형자산 수정
        asset.update(request, null, null);

        return TangibleAssetResponse.from(asset);
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

    private Department resolveDepartment(UUID departmentId, Member member, UUID companyId) {
        if (departmentId != null) {
            return findDepartment(departmentId, companyId);
        }

        return member == null ? null : member.getDepartment();
    }

    private void createAssignmentIfAssigned(
            Company company,
            TangibleAsset asset,
            Member member,
            Department department,
            LocalDateTime usedStartedAt,
            TangibleAssetCreateRequest request
    ) {
        if (member == null) {
            return;
        }

        TangibleAssetAssignment assignment = TangibleAssetAssignment.builder()
                .company(company)
                .tangibleAsset(asset)
                .member(member)
                .department(department)
                .assignmentType(request.getUsageType())
                .assignedAt(usedStartedAt)
                .endedAt(request.getReturnDueDate())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        tangibleAssetAssignmentRepository.save(assignment);
    }

    private void validateUpdateDoesNotChangeAssignment(TangibleAssetUpdateRequest request) {
        if (request.getUsageType() != null ||
                request.getUsedStartedAt() != null ||
                request.getReturnDueDate() != null) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "자산 수정에서는 사용자, 부서, 상태, 사용 유형, 사용 시작일, 반납 예정일을 변경할 수 없습니다."
            );
        }
    }

    private void validateDirectStatusUpdate(TangibleAsset asset, TangibleAssetStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == asset.getTangibleAssetStatus()) {
            return;
        }

        if (TICKET_ONLY_UPDATE_STATUSES.contains(requestedStatus)) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "IN_USE, RETURN_REQUESTED, REPAIR_REQUESTED 상태는 배정, 반납, 수리 요청 흐름을 통해서만 변경할 수 있습니다."
            );
        }

        if (asset.getMember() != null || asset.getDepartment() != null) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "사용자 또는 부서에 배정된 자산은 수정 API에서 상태를 직접 변경할 수 없습니다."
            );
        }
    }

    private TangibleAssetStatus resolveCreateStatus(TangibleAssetCreateRequest request) {
        if (request.getTangibleAssetStatus() == null) {
            return TangibleAssetStatus.AVAILABLE;
        }
        return request.getTangibleAssetStatus();
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
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "사용자의 소속 부서와 자산에 등록되는 부서가 일치하지 않습니다."
            );
        }
    }

    /**
     * AVAILABLE, DISPOSED를 제외한 상태에서는 사용자, 부서, 사용 시작일이 모두 존재하는지 검증한다.
     */
    private void validateRequiredUsageInfo(
            TangibleAssetStatus status,
            Member member,
            Department department,
            LocalDateTime usedStartedAt
    ) {
        if (status == TangibleAssetStatus.AVAILABLE || status == TangibleAssetStatus.DISPOSED) {
            return;
        }

        if (member == null || department == null || usedStartedAt == null) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "AVAILABLE, DISPOSED 상태가 아닌 자산은 사용자, 부서, 사용 시작일이 필요합니다."
            );
        }
    }

    /**
     * 등록 시 반납 예정일 입력 조건을 검증한다.
     */
    private void validateReturnDueDate(LocalDateTime returnDueDate, UsageType usageType, LocalDateTime usedStartedAt) {
        if (returnDueDate == null) {
            return;
        }

        if (usageType == UsageType.PERMANENT) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "영구 사용 자산은 반납 예정일을 입력할 수 없습니다."
            );
        }

        if (usedStartedAt == null) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "반납 예정일을 입력하려면 사용 시작일이 필요합니다."
            );
        }

        if (usedStartedAt.isAfter(returnDueDate)) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "사용 시작일은 반납 예정일보다 늦을 수 없습니다."
            );
        }
    }
}
