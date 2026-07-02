package com.ieumsae.assetieum.domain.intangibleasset.asset.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.csv.CsvFileReader;
import com.ieumsae.assetieum.global.common.csv.CsvValueParser;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;

    private final CodeGenerator codeGenerator;
    private final CsvFileReader csvFileReader;


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

        String licenseCode = normalizeNullableString(request.getLicenseCode());

        if(licenseCode != null && intangibleAssetRepository.existsByCompany_IdAndLicenseCodeAndIntangibleAssetItem_Id(
                companyId,
                licenseCode,
                request.getIntangibleItemId()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
        }

        Member member = findMember(request.getMemberId(), companyId);
        Department department = resolveDepartment(request.getDepartmentId(), member, companyId);
        LocalDateTime startedAt = member != null && request.getStartedAt() == null
                ? KstDateTime.now()
                : request.getStartedAt();
        IntangibleAssetStatus status = member != null ? IntangibleAssetStatus.IN_USE : resolveCreateStatus(request);

        validateMemberDepartment(member, department);
        validateRequiredUsageInfo(status, member, department, startedAt);
        validateExpiredAt(request.getExpiredAt(), request.getBillingCycle(), startedAt);


        // 2. 무형자산 생성 및 저장
        IntangibleAsset asset = IntangibleAsset.builder()
                .company(company)
                .intangibleAssetItem(item)
                .licenseCode(licenseCode)
                .intangibleAssetStatus(status)
                .seatCount(request.getSeatCount())
                .member(member)
                .department(department)
                .assetCode(codeGenerator.generate(INTANGIBLE_ASSET_CODE_PREFIX, REDIS_KEY_PREFIX, companyId))
                .startedAt(startedAt)
                .expiredAt(request.getExpiredAt())
                .isAutoRenewal(request.getIsAutoRenewal())
                .billingCycle(request.getBillingCycle())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseVendor(request.getPurchaseVendor())
                .build();

        IntangibleAsset savedAsset = intangibleAssetRepository.save(asset);
        createAssignmentIfAssigned(company, savedAsset, member, department, startedAt, request);

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

    private Department resolveDepartment(UUID departmentId, Member member, UUID companyId) {
        if (departmentId != null) {
            return findDepartment(departmentId, companyId);
        }

        return member == null ? null : member.getDepartment();
    }

    private void createAssignmentIfAssigned(
            Company company,
            IntangibleAsset asset,
            Member member,
            Department department,
            LocalDateTime startedAt,
            IntangibleAssetCreateRequest request
    ) {
        if (member == null) {
            return;
        }

        IntangibleAssetAssignment assignment = IntangibleAssetAssignment.builder()
                .company(company)
                .intangibleAsset(asset)
                .member(member)
                .department(department)
                .assignedAt(startedAt)
                .endedAt(request.getExpiredAt())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        intangibleAssetAssignmentRepository.save(assignment);
    }

    private void validateUpdateDoesNotChangeAssignment(IntangibleAssetUpdateRequest request) {
        if (request.getIntangibleAssetStatus() != null) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "자산 수정에서는 사용자, 부서, 상태를 변경할 수 없습니다."
            );
        }
    }

    private void validateStartDateUpdate(LocalDateTime currentStartedAt, LocalDateTime requestedStartedAt) {
        if (requestedStartedAt == null) {
            return;
        }

        LocalDateTime now = KstDateTime.now();
        if (currentStartedAt != null && !currentStartedAt.isAfter(now)) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "이미 시작된 자산의 시작일은 수정할 수 없습니다."
            );
        }

        if (!requestedStartedAt.isAfter(now)) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "수정할 시작일은 현재 시점보다 이후여야 합니다."
            );
        }
    }

    private IntangibleAssetStatus resolveCreateStatus(IntangibleAssetCreateRequest request) {
        if (request.getIntangibleAssetStatus() == null) {
            return IntangibleAssetStatus.AVAILABLE;
        }
        return request.getIntangibleAssetStatus();
    }

    private String normalizeNullableString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
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
        if (status == IntangibleAssetStatus.AVAILABLE || status == IntangibleAssetStatus.EXPIRED || status == IntangibleAssetStatus.CANCELLED) {
            return;
        }

        if (member == null || department == null || startedAt == null) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "AVAILABLE, EXPIRED, CANCELLED 상태가 아닌 자산은 사용자, 부서, 사용 시작일이 필요합니다."
            );
        }
    }

    /**
     * 등록 시 만료일 입력 조건을 검증한다.
     */
    private void validateExpiredAt(LocalDateTime expiredAt, BillingCycle billingCycle, LocalDateTime startedAt) {
        if(expiredAt == null) {
            return;
        }

        if(billingCycle == BillingCycle.ONE_TIME){
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "영구 사용 자산은 만료일을 입력할 수 없습니다."
            );
        }

        if (startedAt == null) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "만료일을 입력하려면 사용 시작일이 필요합니다."
            );
        }

        if (startedAt.isAfter(expiredAt)) {
            throw new BusinessException(
                    ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST,
                    "시작일은 만료일보다 늦을 수 없습니다."
            );
        }
    }


    public PaginationResponse<IntangibleAssetSearchResponse> getAssets(
            IntangibleAssetSearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (request.getCategoryId() != null) {
            intangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
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
        Page<IntangibleAssetSearchResponse> assetPage = intangibleAssetRepository.search(
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
     * 무형자산 상세 조회.
     * 회사 범위 내에서 자산 ID에 해당하는 무형자산 상세 정보를 조회한다.
     */
    public IntangibleAssetDetailResponse getAssetDetail(
            UUID assetId,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 무형자산 상세 조회
        return intangibleAssetRepository.findDetailByIdAndCompanyId(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

    }

    /**
     * 무형자산 수정.
     * 요청값과 기존 자산 값을 조합한 최종 상태 기준으로 사용 정보 필수 여부를 검증한다.
     */
    @Transactional
    public IntangibleAssetResponse updateAsset(
            UUID assetId,
            IntangibleAssetUpdateRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        validateUpdateDoesNotChangeAssignment(request);
        validateStartDateUpdate(asset.getStartedAt(), request.getStartedAt());

        LocalDateTime finalStartedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : asset.getStartedAt();

        if (request.getExpiredAt() != null) {
            validateExpiredAt(request.getExpiredAt(), asset.getBillingCycle(), finalStartedAt);
        } else if (request.getStartedAt() != null && asset.getExpiredAt() != null) {
            validateExpiredAt(asset.getExpiredAt(), asset.getBillingCycle(), finalStartedAt);
        }

        // 2. 무형자산 수정
        asset.update(request, null, null);

        return IntangibleAssetResponse.from(asset);
    }

    /**
     * CSV 파일로 유형자산 품목을 일괄 등록한다.
     * 헤더 이후 각 행은 productName,licenseCode,seatCount,isAutoRenewal,purchaseDate, purchasePrice, purchaseVendor, billingCycle 순서여야 한다.
     */
    @Transactional
    public List<IntangibleAssetResponse> importAssets(
            MultipartFile file,
            UUID companyId
    ) {
        List<IntangibleAssetResponse> responses = new ArrayList<>();
        Set<String> licenseCodeKeys = new HashSet<>();

        for(String[] columns : csvFileReader.readRows(file)) {
            if(columns.length != 8) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            IntangibleAssetItem item = intangibleAssetItemRepository.findByProductNameAndCompany_Id(
                        columns[0].trim(),
                        companyId
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

            String licenseCode = CsvValueParser.parseNullableString(columns[1]);
            validateImportLicenseCode(companyId, item.getId(), licenseCode, licenseCodeKeys);

            IntangibleAssetCreateRequest request = IntangibleAssetCreateRequest.builder()
                    .intangibleItemId(item.getId())
                    .licenseCode(licenseCode)
                    .seatCount(CsvValueParser.parseInteger(columns[2]))
                    .isAutoRenewal(CsvValueParser.parseBoolean(columns[3]))
                    .purchaseDate(CsvValueParser.parseDateTime(columns[4]))
                    .purchasePrice(CsvValueParser.parseBigDecimal(columns[5]))
                    .purchaseVendor(columns[6].trim())
                    .billingCycle(CsvValueParser.parseNullableEnum(BillingCycle.class, columns[7]))
                    .build();

            responses.add(createAsset(request, companyId));
        }

        return responses;
    }

    private void validateImportLicenseCode(
            UUID companyId,
            UUID itemId,
            String licenseCode,
            Set<String> licenseCodeKeys
    ) {
        if (licenseCode == null) {
            return;
        }

        String licenseCodeKey = itemId + ":" + licenseCode;
        if (!licenseCodeKeys.add(licenseCodeKey)
                || intangibleAssetRepository.existsByCompany_IdAndLicenseCodeAndIntangibleAssetItem_Id(
                        companyId,
                        licenseCode,
                        itemId
                )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
        }
    }
}
