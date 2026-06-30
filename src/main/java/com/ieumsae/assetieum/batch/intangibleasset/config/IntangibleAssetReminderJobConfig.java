package com.ieumsae.assetieum.batch.intangibleasset.config;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class IntangibleAssetReminderJobConfig {

	public static final String JOB_NAME = "intangibleAssetReminderJob";
	public static final String BASE_DATE_PARAMETER = "baseDate";

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final List<IntangibleAssetStatus> ACTIVE_STATUSES =
		List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE);

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final BudgetExecutionService budgetExecutionService;
	private final NotificationService notificationService;

	@Bean
	public Job intangibleAssetReminderJob() {
		return new JobBuilder(JOB_NAME, jobRepository)
			.start(sendBillingCycleNotificationsStep())
			.next(sendExpirationTomorrowNotificationsStep())
			.next(expireIntangibleAssetsStep())
			.build();
	}

	@Bean
	public Step sendBillingCycleNotificationsStep() {
		return new StepBuilder("sendBillingCycleNotificationsStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate baseDate = resolveBaseDate(chunkContext);
				List<IntangibleAsset> assets = intangibleAssetRepository
					.findAllByIntangibleAssetStatusInAndBillingCycleInAndPurchaseDateIsNotNull(
						ACTIVE_STATUSES,
						List.of(BillingCycle.MONTHLY, BillingCycle.YEARLY)
					);

				assets.stream()
					.filter(asset -> isBillingDate(asset, baseDate))
					.forEach(asset -> processBillingCycle(asset, baseDate));

				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	public Step sendExpirationTomorrowNotificationsStep() {
		return new StepBuilder("sendExpirationTomorrowNotificationsStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate expirationDate = resolveBaseDate(chunkContext).plusDays(1);
				List<IntangibleAsset> assets = findAssetsExpiringOn(expirationDate);

				for (IntangibleAsset asset : assets) {
					sendExpirationTomorrowNotification(asset);
				}

				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	public Step expireIntangibleAssetsStep() {
		return new StepBuilder("expireIntangibleAssetsStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate baseDate = resolveBaseDate(chunkContext);
				List<IntangibleAsset> assets = intangibleAssetRepository
					.findAllByIntangibleAssetStatusInAndExpiredAtLessThan(
						ACTIVE_STATUSES,
						baseDate.plusDays(1).atStartOfDay()
					);
				LocalDateTime expiredAt = LocalDateTime.now();

				for (IntangibleAsset asset : assets) {
					endActiveAssignments(asset, expiredAt);
					asset.expire();
				}

				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	private List<IntangibleAsset> findAssetsExpiringOn(LocalDate expirationDate) {
		LocalDateTime startInclusive = expirationDate.atStartOfDay();
		LocalDateTime endExclusive = startInclusive.plusDays(1);

		return intangibleAssetRepository
			.findAllByIntangibleAssetStatusInAndExpiredAtGreaterThanEqualAndExpiredAtLessThan(
				ACTIVE_STATUSES,
				startInclusive,
				endExclusive
			);
	}

	private boolean isBillingDate(IntangibleAsset asset, LocalDate baseDate) {
		if (asset.getPurchaseDate() == null || asset.getBillingCycle() == null) {
			return false;
		}

		LocalDate purchaseDate = asset.getPurchaseDate().toLocalDate();
		return switch (asset.getBillingCycle()) {
			case MONTHLY -> baseDate.getDayOfMonth() == billingDay(purchaseDate, baseDate);
			case YEARLY -> baseDate.getMonth() == purchaseDate.getMonth()
				&& baseDate.getDayOfMonth() == billingDay(purchaseDate, baseDate);
			case ONE_TIME -> false;
		};
	}

	private int billingDay(LocalDate purchaseDate, LocalDate baseDate) {
		return Math.min(
			purchaseDate.getDayOfMonth(),
			YearMonth.from(baseDate).lengthOfMonth()
		);
	}

	private void sendBillingCycleNotification(IntangibleAsset asset) {
		Member receiver = asset.getMember();
		if (receiver == null || !receiver.isActive()) {
			return;
		}

		notificationService.createNotification(
			receiver,
			NotificationType.INTANGIBLE_ASSET_PAYMENT_DUE,
			"무형자산 결제 주기 알림입니다.",
			createContent("결제 주기가 도래한 무형자산이 있습니다.", asset),
			NotificationTargetType.INTANGIBLE_ASSET,
			asset.getId()
		);
	}

	private void processBillingCycle(IntangibleAsset asset, LocalDate baseDate) {
		if (Boolean.TRUE.equals(asset.getIsAutoRenewal())) {
			budgetExecutionService.executeForIntangibleAssetBillingCycle(asset, baseDate);
		}
		sendBillingCycleNotification(asset);
	}

	private void sendExpirationTomorrowNotification(IntangibleAsset asset) {
		Member receiver = asset.getMember();
		if (receiver == null || !receiver.isActive()) {
			return;
		}

		notificationService.createNotification(
			receiver,
			NotificationType.INTANGIBLE_ASSET_EXPIRING_TOMORROW,
			"무형자산 만료일이 하루 남았습니다.",
			createContent("내일 만료 예정인 무형자산이 있습니다.", asset),
			NotificationTargetType.INTANGIBLE_ASSET,
			asset.getId()
		);
	}

	private String createContent(String summary, IntangibleAsset asset) {
		String itemName = asset.getIntangibleAssetItem() == null
			? "무형자산"
			: asset.getIntangibleAssetItem().getProductName();
		String expiredAt = asset.getExpiredAt() == null
			? "-"
			: asset.getExpiredAt().format(DATE_TIME_FORMATTER);

		return "%s 자산명: %s, 자산번호: %s, 만료일: %s"
			.formatted(summary, itemName, asset.getAssetCode(), expiredAt);
	}

	private void endActiveAssignments(IntangibleAsset asset, LocalDateTime endedAt) {
		intangibleAssetAssignmentRepository
			.findAllByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
				asset.getCompany().getId(),
				asset.getId(),
				AssignmentStatus.ACTIVE
			)
			.forEach(assignment -> assignment.end(endedAt));
	}

	private LocalDate resolveBaseDate(ChunkContext chunkContext) {
		Object parameter = chunkContext.getStepContext()
			.getJobParameters()
			.get(BASE_DATE_PARAMETER);

		if (parameter == null) {
			return LocalDate.now();
		}
		return LocalDate.parse(parameter.toString());
	}
}
