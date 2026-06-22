package com.ieumsae.assetieum.batch.hrevent.config;

import com.ieumsae.assetieum.domain.hr.hrevent.service.HrEventService;
import java.time.LocalDate;
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
public class HrEventExecutionJobConfig {

    // HR 이벤트 실행일에 PENDING 이벤트를 실행하는 Job 이름과 기준일 파라미터 이름입니다.
    public static final String JOB_NAME = "hrEventExecutionJob";
    public static final String HR_EVENT_EXECUTION_DATE_PARAMETER = "hrEventExecutionDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HrEventService hrEventService;

    @Bean
    public Job hrEventExecutionJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                // 실행일이 된 HR 이벤트를 타입별 실행 로직으로 위임합니다.
                .start(executeHrEventsStep())
                .build();
    }

    @Bean
    public Step executeHrEventsStep() {
        return new StepBuilder("executeHrEventsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    hrEventService.executeDueHrEvents(resolveExecutionDate(chunkContext));
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private LocalDate resolveExecutionDate(ChunkContext chunkContext) {
        // 스케줄러가 전달한 기준일을 사용하고, 직접 실행 시 파라미터가 없으면 오늘 날짜를 사용합니다.
        Object parameter = chunkContext.getStepContext()
                .getJobParameters()
                .get(HR_EVENT_EXECUTION_DATE_PARAMETER);

        if (parameter == null) {
            return LocalDate.now();
        }
        return LocalDate.parse(parameter.toString());
    }
}
