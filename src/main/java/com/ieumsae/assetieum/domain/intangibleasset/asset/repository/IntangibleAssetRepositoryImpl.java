package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.department.entity.QDepartment;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.QIntangibleAssetAssignment.intangibleAssetAssignment;
import static com.ieumsae.assetieum.domain.intangibleasset.category.entity.QIntangibleAssetCategory.intangibleAssetCategory;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;

/**
 * 무형자산 Repository 구현체
 * QueryDSL을 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class IntangibleAssetRepositoryImpl implements IntangibleAssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final IntangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 무형자산 목록을 조회한다.
     * 카테고리, 품목 ID, 상태, 키워드, 현재 사용자, 부서 조건을 동적으로 적용한다.
     */
    @Override
    public Page<IntangibleAssetSearchResponse> search(
            UUID companyId,
            UUID categoryId,
            IntangibleAssetStatus status,
            String keyword,
            UUID currentUserId,
            UUID departmentId,
            Pageable pageable
    ) {
        QMember assignedMember = new QMember("assignedMember");
        QDepartment assignedDepartment = new QDepartment("assignedDepartment");

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(intangibleAsset.company.id.eq(companyId));

        List<UUID> categoryIds = getCategoryIds(categoryId, companyId);
        if(categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(intangibleAssetCategory.id.in(categoryIds));
        }

        if(status != null) {
            condition.and(intangibleAsset.intangibleAssetStatus.eq(status));
        }

        if(keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    intangibleAsset.licenseCode.containsIgnoreCase(trimmedKeyword)
                            .or(intangibleAsset.purchaseVendor.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAsset.assetCode.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.provider.containsIgnoreCase(trimmedKeyword))
            );
        }

        if (currentUserId != null) {
            condition.and(
                    member.id.eq(currentUserId)
                            .or(JPAExpressions
                                    .selectOne()
                                    .from(intangibleAssetAssignment)
                                    .where(
                                            intangibleAssetAssignment.intangibleAsset.id.eq(intangibleAsset.id),
                                            intangibleAssetAssignment.assignmentStatus.eq(AssignmentStatus.ACTIVE),
                                            intangibleAssetAssignment.member.id.eq(currentUserId)
                                    )
                                    .exists())
            );
        }

        if (departmentId != null) {
            condition.and(
                    department.id.eq(departmentId)
                            .or(JPAExpressions
                                    .selectOne()
                                    .from(intangibleAssetAssignment)
                                    .where(
                                            intangibleAssetAssignment.intangibleAsset.id.eq(intangibleAsset.id),
                                            intangibleAssetAssignment.assignmentStatus.eq(AssignmentStatus.ACTIVE),
                                            intangibleAssetAssignment.department.id.eq(departmentId)
                                    )
                                    .exists())
            );
        }

        List<Tuple> rows = queryFactory
                .select(
                        intangibleAsset.id,
                        intangibleAssetItem.productName,
                        intangibleAsset.assetCode,
                        member.name,
                        member.memberNo,
                        intangibleAsset.intangibleAssetStatus,
                        department.name
                )
                .from(intangibleAsset)
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .leftJoin(intangibleAsset.member, member)
                .leftJoin(intangibleAsset.department, department)
                .where(condition)
                .orderBy(intangibleAsset.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<UUID> assetIds = rows.stream()
                .map(row -> row.get(intangibleAsset.id))
                .toList();
        Map<UUID, List<ActiveAssignmentUser>> activeAssignmentUsers = findActiveAssignmentUsers(
                assetIds,
                assignedMember,
                assignedDepartment
        );

        List<IntangibleAssetSearchResponse> content = rows.stream()
                .map(row -> {
                    UUID assetId = row.get(intangibleAsset.id);
                    String memberName = row.get(member.name);
                    String memberNo = row.get(member.memberNo);
                    List<ActiveAssignmentUser> users = activeAssignmentUsers.getOrDefault(assetId, List.of());

                    return new IntangibleAssetSearchResponse(
                            assetId,
                            row.get(intangibleAssetItem.productName),
                            row.get(intangibleAsset.assetCode),
                            buildCurrentUserInfo(memberName, memberNo, users),
                            row.get(intangibleAsset.intangibleAssetStatus),
                            buildDepartmentName(row.get(department.name), users)
                    );
                })
                .toList();

        Long total = queryFactory
                .select(intangibleAsset.count())
                .from(intangibleAsset)
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .leftJoin(intangibleAsset.member, member)
                .leftJoin(intangibleAsset.department, department)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);

    }

    @Override
    public Optional<IntangibleAssetDetailResponse> findDetailByIdAndCompanyId(UUID assetId, UUID companyId) {

        IntangibleAssetDetailResponse response = queryFactory
                .select(Projections.constructor(
                        IntangibleAssetDetailResponse.class,
                        intangibleAssetItem.productName,
                        intangibleAsset.assetCode,
                        intangibleAsset.licenseCode,
                        intangibleAsset.intangibleAssetStatus,
                        intangibleAsset.seatCount,
                        intangibleAsset.startedAt,
                        intangibleAsset.expiredAt,
                        intangibleAsset.isAutoRenewal,
                        intangibleAsset.billingCycle,
                        department.name,
                        member.name,
                        intangibleAsset.purchaseDate,
                        intangibleAsset.purchasePrice,
                        intangibleAsset.purchaseVendor
                ))
                .from(intangibleAsset)
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .leftJoin(intangibleAsset.member, member)
                .leftJoin(intangibleAsset.department, department)
                .where(
                        intangibleAsset.id.eq(assetId),
                        intangibleAsset.company.id.eq(companyId)
                )
                .fetchOne();

        return Optional.ofNullable(response);
    }

    private Map<UUID, List<ActiveAssignmentUser>> findActiveAssignmentUsers(
            List<UUID> assetIds,
            QMember assignedMember,
            QDepartment assignedDepartment
    ) {
        if (assetIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
                .select(
                        intangibleAssetAssignment.intangibleAsset.id,
                        assignedMember.name,
                        assignedMember.memberNo,
                        assignedDepartment.name
                )
                .from(intangibleAssetAssignment)
                .join(intangibleAssetAssignment.member, assignedMember)
                .join(intangibleAssetAssignment.department, assignedDepartment)
                .where(
                        intangibleAssetAssignment.intangibleAsset.id.in(assetIds),
                        intangibleAssetAssignment.assignmentStatus.eq(AssignmentStatus.ACTIVE)
                )
                .orderBy(intangibleAssetAssignment.assignedAt.asc())
                .fetch();

        Map<UUID, List<ActiveAssignmentUser>> result = new LinkedHashMap<>();
        for (Tuple row : rows) {
            UUID assetId = row.get(intangibleAssetAssignment.intangibleAsset.id);
            result.computeIfAbsent(assetId, ignored -> new ArrayList<>())
                    .add(new ActiveAssignmentUser(
                            row.get(assignedMember.name),
                            row.get(assignedMember.memberNo),
                            row.get(assignedDepartment.name)
                    ));
        }

        return result;
    }

    private String buildCurrentUserInfo(
            String memberName,
            String memberNo,
            List<ActiveAssignmentUser> activeAssignmentUsers
    ) {
        if (memberName != null) {
            return formatMemberInfo(memberName, memberNo);
        }
        if (activeAssignmentUsers.isEmpty()) {
            return null;
        }

        String firstUserInfo = formatMemberInfo(
                activeAssignmentUsers.get(0).name(),
                activeAssignmentUsers.get(0).memberNo()
        );
        int extraUserCount = activeAssignmentUsers.size() - 1;

        if (extraUserCount == 0) {
            return firstUserInfo;
        }

        return firstUserInfo + "외" + extraUserCount + "명";
    }

    private String buildDepartmentName(
            String departmentName,
            List<ActiveAssignmentUser> activeAssignmentUsers
    ) {
        if (departmentName != null) {
            return departmentName;
        }
        if (activeAssignmentUsers.isEmpty()) {
            return null;
        }

        return activeAssignmentUsers.get(0).departmentName();
    }

    private String formatMemberInfo(String name, String memberNo) {
        return name + "(" + memberNo + ")";
    }

    private List<UUID> getCategoryIds(UUID categoryId, UUID companyId) {
        if(categoryId == null) {
            return null;
        }

        List<UUID> categoryIds = new ArrayList<>(
                categoryRepository.findAllDescendantIds(categoryId, companyId)
        );

        categoryIds.add(categoryId);

        return categoryIds;
    }

    private record ActiveAssignmentUser(
            String name,
            String memberNo,
            String departmentName
    ) {
    }
}
