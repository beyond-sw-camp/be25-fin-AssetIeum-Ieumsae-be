package com.ieumsae.assetieum.batch.inspection.config;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.repository.InspectionRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.service.InspectionService;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class InspectionEndJobConfig {

    // 전수조사 종료일 23:59:59에 실행되는 Job 이름과 기준일 파라미터 이름입니다.
    public static final String JOB_NAME = "inspectionEndJob";
    public static final String INSPECTION_END_DATE_PARAMETER = "inspectionEndDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InspectionRepository inspectionRepository;
    private final InspectionService inspectionService;

    @Bean
    public Job inspectionEndJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                // Step 1. 종료일이 된 진행 중 전수조사를 완료 상태로 전환합니다.
                .start(completeInspectionsStep())
                .build();
    }

    @Bean
    public Step completeInspectionsStep() {
        return new StepBuilder("completeInspectionsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<Inspection> inspections = findInProgressInspections(resolveEndDate(chunkContext));
                    inspections.forEach(inspection -> {
                        inspection.complete();
                        inspectionService.closeIfCompletedAndAllFollowUpsCompleted(inspection);
                    });
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private List<Inspection> findInProgressInspections(LocalDate endDate) {
        // LocalDate 기준으로 해당 날짜의 00:00 이상, 다음 날 00:00 미만 범위를 조회합니다.
        LocalDateTime startInclusive = endDate.atStartOfDay();
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        return inspectionRepository.findAllByInspectionStatusAndEndDateGreaterThanEqualAndEndDateLessThan(
                InspectionStatus.IN_PROGRESS,
                startInclusive,
                endExclusive
        );
    }

    private LocalDate resolveEndDate(ChunkContext chunkContext) {
        // 스케줄러가 전달한 기준일을 사용하고, 직접 실행 시 파라미터가 없으면 오늘 날짜를 사용합니다.
        Object parameter = chunkContext.getStepContext()
                .getJobParameters()
                .get(INSPECTION_END_DATE_PARAMETER);

        if (parameter == null) {
            return KstDateTime.today();
        }
        return LocalDate.parse(parameter.toString());
    }
}
