package com.ieumsae.assetieum.domain.intangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.QIntangibleAssetAssignment.intangibleAssetAssignment;

@Repository
@RequiredArgsConstructor
public class IntangibleAssetAssignmentRepositoryImpl implements IntangibleAssetAssignmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IntangibleAssetAssignmentSearchResponse> search(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(intangibleAssetAssignment.company.id.eq(companyId));
        condition.and(intangibleAssetAssignment.intangibleAsset.id.eq(assetId));

        if (assignmentStatus != null) {
            condition.and(intangibleAssetAssignment.assignmentStatus.eq(assignmentStatus));
        }

        return queryFactory
                .selectFrom(intangibleAssetAssignment)
                .join(intangibleAssetAssignment.member).fetchJoin()
                .join(intangibleAssetAssignment.department).fetchJoin()
                .where(condition)
                .orderBy(
                        intangibleAssetAssignment.assignedAt.desc(),
                        intangibleAssetAssignment.createdAt.desc()
                )
                .fetch()
                .stream()
                .map(IntangibleAssetAssignmentSearchResponse::from)
                .collect(Collectors.toList());
    }
}
