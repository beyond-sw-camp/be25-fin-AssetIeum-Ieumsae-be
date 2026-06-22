package com.ieumsae.assetieum.batch.inspection.config;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class InspectionEndingReminderJobConfig {

    // 전수조사 종료 전날 10시에 실행되는 Job 이름과 종료 기준일 파라미터 이름입니다.
    public static final String JOB_NAME = "inspectionEndingReminderJob";
    public static final String INSPECTION_END_DATE_PARAMETER = "inspectionEndDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InspectionTargetRepository inspectionTargetRepository;
    private final NotificationService notificationService;

    @Bean
    public Job inspectionEndingReminderJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                // 종료일이 내일이고 아직 응답하지 않은 target 담당자에게 리마인드 알림을 보냅니다.
                .start(sendInspectionEndingReminderStep())
                .build();
    }

    @Bean
    public Step sendInspectionEndingReminderStep() {
        return new StepBuilder("sendInspectionEndingReminderStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<InspectionTarget> targets = findUnrespondedTargets(resolveEndDate(chunkContext));
                    Set<NotificationKey> sentKeys = new HashSet<>();

                    for (InspectionTarget target : targets) {
                        Inspection inspection = target.getInspection();
                        Member receiver = target.getMember();
                        if (receiver == null || !receiver.isActive()) {
                            continue;
                        }

                        // 같은 전수조사에서 한 담당자가 미응답 target을 여러 개 가져도 알림은 한 번만 발송합니다.
                        NotificationKey key = new NotificationKey(inspection.getId(), receiver.getId());
                        if (!sentKeys.add(key)) {
                            continue;
                        }

                        notificationService.createNotification(
                                receiver,
                                NotificationType.INSPECTION_ENDING,
                                "전수조사 종료일이 하루 남았습니다.",
                                "아직 응답하지 않은 전수조사 대상 자산이 있습니다. 종료일 전까지 점검 결과를 등록해 주세요.",
                                NotificationTargetType.INSPECTION,
                                inspection.getId()
                        );
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private List<InspectionTarget> findUnrespondedTargets(LocalDate endDate) {
        // 종료 기준일의 00:00 이상, 다음 날 00:00 미만 범위에 속한 IN_PROGRESS 전수조사를 조회합니다.
        LocalDateTime startInclusive = endDate.atStartOfDay();
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        return inspectionTargetRepository.findAllUnrespondedTargetsForEndingReminder(
                InspectionStatus.IN_PROGRESS,
                startInclusive,
                endExclusive
        );
    }

    private LocalDate resolveEndDate(ChunkContext chunkContext) {
        // 스케줄러는 내일 날짜를 전달하고, 직접 실행 시 파라미터가 없으면 내일 날짜를 사용합니다.
        Object parameter = chunkContext.getStepContext()
                .getJobParameters()
                .get(INSPECTION_END_DATE_PARAMETER);

        if (parameter == null) {
            return LocalDate.now().plusDays(1);
        }
        return LocalDate.parse(parameter.toString());
    }

    private record NotificationKey(UUID inspectionId, UUID receiverId) {
    }
}
