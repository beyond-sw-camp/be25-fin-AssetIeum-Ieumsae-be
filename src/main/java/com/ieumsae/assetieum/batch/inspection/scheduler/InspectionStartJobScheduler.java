package com.ieumsae.assetieum.batch.inspection.scheduler;

import com.ieumsae.assetieum.batch.inspection.config.InspectionStartJobConfig;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InspectionStartJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job inspectionStartJob;

    // 매일 자정에 오늘 시작일인 전수조사를 대상으로 inspectionStartJob을 실행합니다.
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void run() throws Exception {
        // JobParameters가 같으면 Spring Batch가 같은 JobInstance로 판단하므로 실행 기준일을 명시합니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(
                        InspectionStartJobConfig.INSPECTION_START_DATE_PARAMETER,
                        LocalDate.now().toString()
                )
                .toJobParameters();

        jobLauncher.run(inspectionStartJob, jobParameters);
    }
}
