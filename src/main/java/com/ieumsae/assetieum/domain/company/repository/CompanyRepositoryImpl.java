package com.ieumsae.assetieum.domain.company.repository;

import com.ieumsae.assetieum.domain.company.dto.CompanySearchResponse;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ieumsae.assetieum.domain.company.entity.QCompany.company;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CompanySearchResponse> search(String keyword, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(company.deletedAt.isNull());

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    company.companyCode.containsIgnoreCase(trimmedKeyword)
                            .or(company.companyName.containsIgnoreCase(trimmedKeyword))
            );
        }

        List<CompanySearchResponse> content = queryFactory
                .select(Projections.constructor(
                        CompanySearchResponse.class,
                        company.id,
                        company.companyCode,
                        company.companyName,
                        new CaseBuilder()
                                .when(member.id.count().gt(0L))
                                .then(member.id.count().intValue().subtract(1))
                                .otherwise(0),
                        company.createdAt,
                        company.updatedAt
                ))
                .from(company)
                .leftJoin(member).on(
                        member.company.eq(company),
                        member.status.eq(MemberStatus.ACTIVE),
                        member.deletedAt.isNull()
                )
                .where(condition)
                .groupBy(company.id)
                .orderBy(company.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
