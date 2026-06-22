package com.ieumsae.assetieum.batch.inspection.scheduler;

import com.ieumsae.assetieum.batch.inspection.config.InspectionEndingReminderJobConfig;
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
public class InspectionEndingReminderJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job inspectionEndingReminderJob;

    // 매일 10시에 내일 종료일인 전수조사의 미응답 담당자에게 알림을 보냅니다.
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void run() throws Exception {
        // JobParameters가 같으면 Spring Batch가 같은 JobInstance로 판단하므로 종료 기준일을 명시합니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(
                        InspectionEndingReminderJobConfig.INSPECTION_END_DATE_PARAMETER,
                        LocalDate.now().plusDays(1).toString()
                )
                .toJobParameters();

        jobLauncher.run(inspectionEndingReminderJob, jobParameters);
    }
}
