package com.ieumsae.assetieum.batch.inspection.scheduler;

import com.ieumsae.assetieum.batch.inspection.config.InspectionEndJobConfig;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InspectionEndJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job inspectionEndJob;

    // 매일 23:59:59에 오늘 종료일인 전수조사를 대상으로 inspectionEndJob을 실행합니다.
    @Scheduled(cron = "59 59 23 * * *", zone = "Asia/Seoul")
    public void run() throws Exception {
        // JobParameters가 같으면 Spring Batch가 같은 JobInstance로 판단하므로 실행 기준일을 명시합니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(
                        InspectionEndJobConfig.INSPECTION_END_DATE_PARAMETER,
                        LocalDate.now().toString()
                )
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(inspectionEndJob, jobParameters);
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException ignored) {
        }
    }
}
