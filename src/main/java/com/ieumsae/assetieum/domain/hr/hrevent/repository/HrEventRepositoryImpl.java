package com.ieumsae.assetieum.domain.hr.hrevent.repository;

import com.ieumsae.assetieum.domain.department.entity.QDepartment;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.hr.hrevent.entity.QHrEvent.hrEvent;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;

/**
 * HR 이벤트 Repository 구현체
 * QueryDSL 을 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class HrEventRepositoryImpl implements HrEventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HrEventResponse> search(
            UUID companyId,
            UUID departmentId,
            HrEventStatus hrEventStatus,
            HrEventType hrEventType,
            Pageable pageable
    ) {

        QDepartment targetDepartment = new QDepartment("targetDepartment");
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(hrEvent.company.id.eq(companyId));
        condition.and(hrEvent.department.id.eq(departmentId));

        if(hrEventStatus != null) {
            condition.and(hrEvent.hrEventStatus.eq(hrEventStatus));
        }

        if(hrEventType != null) {
            condition.and(hrEvent.eventType.eq(hrEventType));
        }

        List<HrEventResponse> content = queryFactory
                .select(Projections.constructor(
                        HrEventResponse.class,
                        hrEvent.id,
                        hrEvent.hrEventNo,
                        hrEvent.department.id,
                        hrEvent.department.name,
                        targetDepartment.id,
                        targetDepartment.name,
                        hrEvent.member.id,
                        hrEvent.member.name,
                        hrEvent.hrEventStatus,
                        hrEvent.eventType,
                        hrEvent.eventDate,
                        hrEvent.executedAt,
                        hrEvent.completedAt,
                        hrEvent.cancelledAt,
                        hrEvent.createdAt,
                        hrEvent.updatedAt
                ))
                .from(hrEvent)
                .join(hrEvent.department, department)
                .leftJoin(hrEvent.targetDepartment, targetDepartment)
                .join(hrEvent.member, member)
                .where(condition)
                .orderBy(hrEvent.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(hrEvent.count())
                .from(hrEvent)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
