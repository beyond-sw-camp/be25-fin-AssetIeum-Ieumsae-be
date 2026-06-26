package com.ieumsae.assetieum.domain.ticket.common.repository;

import static com.ieumsae.assetieum.domain.ticket.assetrequest.entity.QAssetRequestTicket.assetRequestTicket;
import static com.ieumsae.assetieum.domain.ticket.assetreturn.entity.QAssetReturnTicket.assetReturnTicket;
import static com.ieumsae.assetieum.domain.ticket.common.entity.QTicket.ticket;
import static com.ieumsae.assetieum.domain.ticket.maintenance.entity.QMaintenanceTicket.maintenanceTicket;
import static com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.QPurchaseRequestTicket.purchaseRequestTicket;
import static com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.QPurchaseReturnTicket.purchaseReturnTicket;
import static com.ieumsae.assetieum.domain.ticket.rental.entity.QRentalTicket.rentalTicket;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<TicketListItemResponse> searchTickets(UUID companyId, TicketSearchRequest request) {
		QTangibleAssetItem assetRequestTangibleItem = new QTangibleAssetItem("assetRequestTangibleItem");
		QIntangibleAssetItem assetRequestIntangibleItem = new QIntangibleAssetItem("assetRequestIntangibleItem");
		QTangibleAssetItem rentalTangibleItem = new QTangibleAssetItem("rentalTangibleItem");
		QTangibleAsset maintenanceTangibleAsset = new QTangibleAsset("maintenanceTangibleAsset");
		QTangibleAssetItem maintenanceTangibleItem = new QTangibleAssetItem("maintenanceTangibleItem");
		QTangibleAsset assetReturnTangibleAsset = new QTangibleAsset("assetReturnTangibleAsset");
		QTangibleAssetItem assetReturnTangibleItem = new QTangibleAssetItem("assetReturnTangibleItem");
		QIntangibleAsset assetReturnIntangibleAsset = new QIntangibleAsset("assetReturnIntangibleAsset");
		QIntangibleAssetItem assetReturnIntangibleItem = new QIntangibleAssetItem("assetReturnIntangibleItem");
		QTangibleAsset purchaseReturnTangibleAsset = new QTangibleAsset("purchaseReturnTangibleAsset");
		QTangibleAssetItem purchaseReturnTangibleItem = new QTangibleAssetItem("purchaseReturnTangibleItem");
		QIntangibleAsset purchaseReturnIntangibleAsset = new QIntangibleAsset("purchaseReturnIntangibleAsset");
		QIntangibleAssetItem purchaseReturnIntangibleItem = new QIntangibleAssetItem("purchaseReturnIntangibleItem");

		BooleanBuilder condition = new BooleanBuilder();
		condition.and(ticket.company.id.eq(companyId));
		condition.and(ticket.deletedAt.isNull());

		if (request.getTicketStatus() != null) {
			condition.and(ticket.ticketStatus.eq(request.getTicketStatus()));
		}

		if (request.getTicketType() != null) {
			condition.and(ticket.ticketType.eq(request.getTicketType()));
		}

		if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
			BooleanBuilder scopeCondition = new BooleanBuilder();
			scopeCondition.or(ticket.department.id.in(request.getDepartmentIds()));
			if (request.getApproverId() != null) {
				scopeCondition.or(ticket.approver.id.eq(request.getApproverId()));
			}
			condition.and(scopeCondition);
		} else if (request.getDepartmentId() != null) {
			condition.and(ticket.department.id.eq(request.getDepartmentId()));
		} else if (request.getApproverId() != null) {
			condition.and(ticket.approver.id.eq(request.getApproverId()));
		}

		if (request.getRequesterId() != null) {
			condition.and(ticket.requester.id.eq(request.getRequesterId()));
		}

		if (StringUtils.hasText(request.getKeyword())) {
			String keyword = request.getKeyword().trim();
			condition.and(
				ticket.ticketNo.containsIgnoreCase(keyword)
					.or(assetRequestTangibleItem.productName.containsIgnoreCase(keyword))
					.or(assetRequestIntangibleItem.productName.containsIgnoreCase(keyword))
					.or(purchaseRequestTicket.requestedItemDetail.containsIgnoreCase(keyword))
					.or(rentalTangibleItem.productName.containsIgnoreCase(keyword))
					.or(maintenanceTangibleItem.productName.containsIgnoreCase(keyword))
					.or(assetReturnTangibleItem.productName.containsIgnoreCase(keyword))
					.or(assetReturnIntangibleItem.productName.containsIgnoreCase(keyword))
					.or(purchaseReturnTangibleItem.productName.containsIgnoreCase(keyword))
					.or(purchaseReturnIntangibleItem.productName.containsIgnoreCase(keyword))
			);
		}

		Pageable pageable = request.toPageable();
		Expression<String> requestedItemName = Expressions.stringTemplate(
			"coalesce({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8})",
			assetRequestTangibleItem.productName,
			assetRequestIntangibleItem.productName,
			purchaseRequestTicket.requestedItemDetail,
			rentalTangibleItem.productName,
			maintenanceTangibleItem.productName,
			assetReturnTangibleItem.productName,
			assetReturnIntangibleItem.productName,
			purchaseReturnTangibleItem.productName,
			purchaseReturnIntangibleItem.productName
		);

		List<TicketListItemResponse> content = queryFactory
			.select(Projections.constructor(
				TicketListItemResponse.class,
				ticket.id,
				ticket.ticketNo,
				ticket.ticketType,
				purchaseRequestTicket.requestMethod,
				requestedItemName,
				ticket.requester.id,
				ticket.requester.name,
				ticket.department.id,
				ticket.department.name,
				ticket.createdAt,
				ticket.ticketStatus
			))
			.from(ticket)
			.leftJoin(assetRequestTicket).on(
				assetRequestTicket.ticket.eq(ticket)
					.and(assetRequestTicket.deletedAt.isNull())
			)
			.leftJoin(assetRequestTicket.tangibleAssetItem, assetRequestTangibleItem)
			.leftJoin(assetRequestTicket.intangibleAssetItem, assetRequestIntangibleItem)
			.leftJoin(purchaseRequestTicket).on(
				purchaseRequestTicket.ticket.eq(ticket)
					.and(purchaseRequestTicket.deletedAt.isNull())
			)
			.leftJoin(rentalTicket).on(
				rentalTicket.ticket.eq(ticket)
					.and(rentalTicket.deletedAt.isNull())
			)
			.leftJoin(rentalTicket.tangibleAssetItem, rentalTangibleItem)
			.leftJoin(maintenanceTicket).on(
				maintenanceTicket.ticket.eq(ticket)
					.and(maintenanceTicket.deletedAt.isNull())
			)
			.leftJoin(maintenanceTicket.tangibleAsset, maintenanceTangibleAsset)
			.leftJoin(maintenanceTangibleAsset.tangibleAssetItem, maintenanceTangibleItem)
			.leftJoin(assetReturnTicket).on(
				assetReturnTicket.ticket.eq(ticket)
					.and(assetReturnTicket.deletedAt.isNull())
			)
			.leftJoin(assetReturnTicket.tangibleAsset, assetReturnTangibleAsset)
			.leftJoin(assetReturnTangibleAsset.tangibleAssetItem, assetReturnTangibleItem)
			.leftJoin(assetReturnTicket.intangibleAsset, assetReturnIntangibleAsset)
			.leftJoin(assetReturnIntangibleAsset.intangibleAssetItem, assetReturnIntangibleItem)
			.leftJoin(purchaseReturnTicket).on(
				purchaseReturnTicket.ticket.eq(ticket)
					.and(purchaseReturnTicket.deletedAt.isNull())
			)
			.leftJoin(purchaseReturnTicket.tangibleAsset, purchaseReturnTangibleAsset)
			.leftJoin(purchaseReturnTangibleAsset.tangibleAssetItem, purchaseReturnTangibleItem)
			.leftJoin(purchaseReturnTicket.intangibleAsset, purchaseReturnIntangibleAsset)
			.leftJoin(purchaseReturnIntangibleAsset.intangibleAssetItem, purchaseReturnIntangibleItem)
			.where(condition)
			.orderBy(ticket.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(ticket.count())
			.from(ticket)
			.leftJoin(assetRequestTicket).on(
				assetRequestTicket.ticket.eq(ticket)
					.and(assetRequestTicket.deletedAt.isNull())
			)
			.leftJoin(assetRequestTicket.tangibleAssetItem, assetRequestTangibleItem)
			.leftJoin(assetRequestTicket.intangibleAssetItem, assetRequestIntangibleItem)
			.leftJoin(purchaseRequestTicket).on(
				purchaseRequestTicket.ticket.eq(ticket)
					.and(purchaseRequestTicket.deletedAt.isNull())
			)
			.leftJoin(rentalTicket).on(
				rentalTicket.ticket.eq(ticket)
					.and(rentalTicket.deletedAt.isNull())
			)
			.leftJoin(rentalTicket.tangibleAssetItem, rentalTangibleItem)
			.leftJoin(maintenanceTicket).on(
				maintenanceTicket.ticket.eq(ticket)
					.and(maintenanceTicket.deletedAt.isNull())
			)
			.leftJoin(maintenanceTicket.tangibleAsset, maintenanceTangibleAsset)
			.leftJoin(maintenanceTangibleAsset.tangibleAssetItem, maintenanceTangibleItem)
			.leftJoin(assetReturnTicket).on(
				assetReturnTicket.ticket.eq(ticket)
					.and(assetReturnTicket.deletedAt.isNull())
			)
			.leftJoin(assetReturnTicket.tangibleAsset, assetReturnTangibleAsset)
			.leftJoin(assetReturnTangibleAsset.tangibleAssetItem, assetReturnTangibleItem)
			.leftJoin(assetReturnTicket.intangibleAsset, assetReturnIntangibleAsset)
			.leftJoin(assetReturnIntangibleAsset.intangibleAssetItem, assetReturnIntangibleItem)
			.leftJoin(purchaseReturnTicket).on(
				purchaseReturnTicket.ticket.eq(ticket)
					.and(purchaseReturnTicket.deletedAt.isNull())
			)
			.leftJoin(purchaseReturnTicket.tangibleAsset, purchaseReturnTangibleAsset)
			.leftJoin(purchaseReturnTangibleAsset.tangibleAssetItem, purchaseReturnTangibleItem)
			.leftJoin(purchaseReturnTicket.intangibleAsset, purchaseReturnIntangibleAsset)
			.leftJoin(purchaseReturnIntangibleAsset.intangibleAssetItem, purchaseReturnIntangibleItem)
			.where(condition)
			.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	@Override
	public TicketStatisticsResponse getTicketStatistics(
		UUID companyId,
		List<UUID> departmentIds,
		UUID requesterId,
		UUID approverId
	) {
		BooleanBuilder condition = new BooleanBuilder();
		condition.and(ticket.company.id.eq(companyId));
		condition.and(ticket.deletedAt.isNull());

		if (departmentIds != null && !departmentIds.isEmpty()) {
			BooleanBuilder scopeCondition = new BooleanBuilder();
			scopeCondition.or(ticket.department.id.in(departmentIds));
			if (approverId != null) {
				scopeCondition.or(ticket.approver.id.eq(approverId));
			}
			condition.and(scopeCondition);
		} else if (approverId != null) {
			condition.and(ticket.approver.id.eq(approverId));
		}

		if (requesterId != null) {
			condition.and(ticket.requester.id.eq(requesterId));
		}

		NumberExpression<Long> newOrPendingReviewCount = statusCount(
			TicketStatus.REQUESTED,
			TicketStatus.DEPARTMENT_APPROVED
		);
		NumberExpression<Long> inProgressCount = statusCount(TicketStatus.IN_PROGRESS);
		NumberExpression<Long> completedCount = statusCount(TicketStatus.COMPLETED);

		Tuple result = queryFactory
			.select(
				ticket.count(),
				newOrPendingReviewCount,
				inProgressCount,
				completedCount
			)
			.from(ticket)
			.where(condition)
			.fetchOne();

		if (result == null) {
			return TicketStatisticsResponse.builder()
				.totalCount(0)
				.newOrPendingReviewCount(0)
				.inProgressCount(0)
				.completedCount(0)
				.build();
		}

		return TicketStatisticsResponse.builder()
			.totalCount(toLong(result.get(ticket.count())))
			.newOrPendingReviewCount(toLong(result.get(newOrPendingReviewCount)))
			.inProgressCount(toLong(result.get(inProgressCount)))
			.completedCount(toLong(result.get(completedCount)))
			.build();
	}

	private NumberExpression<Long> statusCount(TicketStatus... statuses) {
		return new CaseBuilder()
			.when(ticket.ticketStatus.in(statuses))
			.then(1L)
			.otherwise(0L)
			.sum();
	}

	private long toLong(Long value) {
		return value == null ? 0 : value;
	}
}
