package com.ieumsae.assetieum.batch.inspection.config;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.repository.InspectionRepository;
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
public class InspectionStartJobConfig {

    // 전수조사 시작일 00시에 실행되는 Job 이름과 기준일 파라미터 이름입니다.
    public static final String JOB_NAME = "inspectionStartJob";
    public static final String INSPECTION_START_DATE_PARAMETER = "inspectionStartDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InspectionRepository inspectionRepository;
    private final InspectionTargetRepository inspectionTargetRepository;
    private final NotificationService notificationService;

    @Bean
    public Job inspectionStartJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                // Step 1. 전수조사 대상자로 생성된 사용자에게 시작 알림을 발송합니다.
                .start(sendInspectionStartNotificationsStep())

                // Step 2. 알림 발송 후 전수조사 상태를 IN_PROGRESS로 변경합니다.
                .next(startInspectionsStep())

                .build();
    }

    @Bean
    public Step sendInspectionStartNotificationsStep() {
        return new StepBuilder("sendInspectionStartNotificationsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 기준일에 시작하고 아직 READY 상태인 전수조사만 알림 발송 대상으로 삼습니다.
                    List<Inspection> inspections = findReadyInspections(resolveStartDate(chunkContext));
                    if (inspections.isEmpty()) {
                        return RepeatStatus.FINISHED;
                    }

                    List<UUID> inspectionIds = inspections.stream()
                            .map(Inspection::getId)
                            .toList();
                    List<InspectionTarget> targets = inspectionTargetRepository
                            .findAllNotificationTargetsByInspectionIdIn(inspectionIds);
                    Set<NotificationKey> sentKeys = new HashSet<>();

                    for (InspectionTarget target : targets) {
                        Inspection inspection = target.getInspection();
                        Member receiver = target.getMember();
                        if (receiver == null || !receiver.isActive()) {
                            continue;
                        }

                        // 한 사용자가 같은 전수조사에 여러 target을 가져도 시작 알림은 한 번만 발송
                        NotificationKey key = new NotificationKey(inspection.getId(), receiver.getId());
                        if (!sentKeys.add(key)) {
                            continue;
                        }

                        notificationService.createNotification(
                                receiver,
                                NotificationType.INSPECTION_STARTED,
                                "전수조사가 시작되었습니다.",
                                "전수조사 대상 자산을 확인하고 기간 내 점검 결과를 등록해 주세요.",
                                NotificationTargetType.INSPECTION,
                                inspection.getId()
                        );
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startInspectionsStep() {
        return new StepBuilder("startInspectionsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 알림 발송이 끝난 전수조사를 진행 중 상태로 전환합니다.
                    List<Inspection> inspections = findReadyInspections(resolveStartDate(chunkContext));
                    inspections.forEach(Inspection::start);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private List<Inspection> findReadyInspections(LocalDate startDate) {
        // LocalDate 기준으로 해당 날짜의 00:00 이상, 다음 날 00:00 미만 범위를 조회합니다.
        LocalDateTime startInclusive = startDate.atStartOfDay();
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        return inspectionRepository.findAllByInspectionStatusAndStartDateGreaterThanEqualAndStartDateLessThan(
                InspectionStatus.READY,
                startInclusive,
                endExclusive
        );
    }

    private LocalDate resolveStartDate(ChunkContext chunkContext) {
        // 스케줄러가 전달한 기준일을 사용하고, 직접 실행 시 파라미터가 없으면 오늘 날짜를 사용합니다.
        Object parameter = chunkContext.getStepContext()
                .getJobParameters()
                .get(INSPECTION_START_DATE_PARAMETER);

        if (parameter == null) {
            return LocalDate.now();
        }
        return LocalDate.parse(parameter.toString());
    }

    private record NotificationKey(UUID inspectionId, UUID receiverId) {
    }
}
