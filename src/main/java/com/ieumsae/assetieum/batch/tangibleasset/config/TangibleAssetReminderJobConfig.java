package com.ieumsae.assetieum.batch.tangibleasset.config;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
public class TangibleAssetReminderJobConfig {

	public static final String JOB_NAME = "tangibleAssetReminderJob";
	public static final String BASE_DATE_PARAMETER = "baseDate";

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final NotificationService notificationService;

	@Bean
	public Job tangibleAssetReminderJob() {
		return new JobBuilder(JOB_NAME, jobRepository)
			.start(sendRentalReturnDueTomorrowReminderStep())
			.next(sendRentalReturnDueTodayReminderStep())
			.next(sendRentalReturnOverdueReminderStep())
			.build();
	}

	@Bean
	public Step sendRentalReturnDueTomorrowReminderStep() {
		return new StepBuilder("sendRentalReturnDueTomorrowReminderStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate baseDate = resolveBaseDate(chunkContext);
				List<TangibleAssetAssignment> assignments = findAssignmentsDueOn(baseDate.plusDays(1));

				sendNotifications(
					assignments,
					NotificationType.RENTAL_RETURN_DUE_TOMORROW,
					"대여 자산 반납일이 하루 남았습니다.",
					"내일 반납 예정인 대여 자산이 있습니다."
				);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	public Step sendRentalReturnDueTodayReminderStep() {
		return new StepBuilder("sendRentalReturnDueTodayReminderStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate baseDate = resolveBaseDate(chunkContext);
				List<TangibleAssetAssignment> assignments = findAssignmentsDueOn(baseDate);

				sendNotifications(
					assignments,
					NotificationType.RENTAL_RETURN_DUE_TODAY,
					"대여 자산 반납일입니다.",
					"오늘 반납 예정인 대여 자산이 있습니다."
				);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	public Step sendRentalReturnOverdueReminderStep() {
		return new StepBuilder("sendRentalReturnOverdueReminderStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDate baseDate = resolveBaseDate(chunkContext);
				List<TangibleAssetAssignment> assignments = findOverdueAssignments(baseDate);

				sendNotifications(
					assignments,
					NotificationType.RENTAL_RETURN_OVERDUE,
					"대여 자산 반납일이 지났습니다.",
					"반납 예정일이 지난 대여 자산이 있습니다."
				);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	private List<TangibleAssetAssignment> findAssignmentsDueOn(LocalDate dueDate) {
		LocalDateTime startInclusive = dueDate.atStartOfDay();
		LocalDateTime endExclusive = startInclusive.plusDays(1);

		return tangibleAssetAssignmentRepository
			.findAllByAssignmentStatusAndAssignmentTypeAndTangibleAsset_TangibleAssetStatusAndTangibleAsset_ReturnDueDateGreaterThanEqualAndTangibleAsset_ReturnDueDateLessThan(
				AssignmentStatus.ACTIVE,
				UsageType.TEMPORARY,
				TangibleAssetStatus.IN_USE,
				startInclusive,
				endExclusive
			);
	}

	private List<TangibleAssetAssignment> findOverdueAssignments(LocalDate baseDate) {
		return tangibleAssetAssignmentRepository
			.findAllByAssignmentStatusAndAssignmentTypeAndTangibleAsset_TangibleAssetStatusAndTangibleAsset_ReturnDueDateLessThan(
				AssignmentStatus.ACTIVE,
				UsageType.TEMPORARY,
				TangibleAssetStatus.IN_USE,
				baseDate.atStartOfDay()
			);
	}

	private void sendNotifications(
		List<TangibleAssetAssignment> assignments,
		NotificationType notificationType,
		String title,
		String summary
	) {
		Set<NotificationKey> sentKeys = new HashSet<>();

		for (TangibleAssetAssignment assignment : assignments) {
			Member receiver = assignment.getMember();
			TangibleAsset asset = assignment.getTangibleAsset();
			if (receiver == null || !receiver.isActive() || asset == null || asset.getReturnDueDate() == null) {
				continue;
			}

			NotificationKey key = new NotificationKey(asset.getId(), receiver.getId(), notificationType);
			if (!sentKeys.add(key)) {
				continue;
			}

			notificationService.createNotification(
				receiver,
				notificationType,
				title,
				createContent(summary, asset),
				NotificationTargetType.TANGIBLE_ASSET,
				asset.getId()
			);
		}
	}

	private String createContent(String summary, TangibleAsset asset) {
		String itemName = asset.getTangibleAssetItem() == null
			? "대여 자산"
			: asset.getTangibleAssetItem().getProductName();

		return "%s 자산명: %s, 자산번호: %s, 반납 예정일: %s"
			.formatted(
				summary,
				itemName,
				asset.getAssetCode(),
				asset.getReturnDueDate().format(DATE_TIME_FORMATTER)
			);
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

	private record NotificationKey(UUID assetId, UUID receiverId, NotificationType notificationType) {
	}
}
