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

    public static final String JOB_NAME = "hrEventExecutionJob";
    public static final String HR_EVENT_EXECUTION_DATE_PARAMETER = "hrEventExecutionDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HrEventService hrEventService;

    @Bean
    public Job hrEventExecutionJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(executeHrEventsStep())
                .next(completeOffboardingHrEventsStep())
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

    @Bean
    public Step completeOffboardingHrEventsStep() {
        return new StepBuilder("completeOffboardingHrEventsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    hrEventService.completeDueOffboardingHrEvents(resolveExecutionDate(chunkContext));
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private LocalDate resolveExecutionDate(ChunkContext chunkContext) {
        Object parameter = chunkContext.getStepContext()
                .getJobParameters()
                .get(HR_EVENT_EXECUTION_DATE_PARAMETER);

        if (parameter == null) {
            return LocalDate.now();
        }
        return LocalDate.parse(parameter.toString());
    }
}
