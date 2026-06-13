package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.QTangibleAssetAssignment.tangibleAssetAssignment;

@Repository
@RequiredArgsConstructor
public class TangibleAssetAssignmentRepositoryImpl implements TangibleAssetAssignmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TangibleAssetAssignmentSearchResponse> search(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(tangibleAssetAssignment.company.id.eq(companyId));
        condition.and(tangibleAssetAssignment.tangibleAsset.id.eq(assetId));

        if (assignmentStatus != null) {
            condition.and(tangibleAssetAssignment.assignmentStatus.eq(assignmentStatus));
        }

        return queryFactory
                .selectFrom(tangibleAssetAssignment)
                .join(tangibleAssetAssignment.member).fetchJoin()
                .join(tangibleAssetAssignment.department).fetchJoin()
                .where(condition)
                .orderBy(
                        tangibleAssetAssignment.assignedAt.desc(),
                        tangibleAssetAssignment.createdAt.desc()
                )
                .fetch()
                .stream()
                .map(TangibleAssetAssignmentSearchResponse::from)
                .collect(Collectors.toList());
    }
}
